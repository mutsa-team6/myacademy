package com.project.myacademy.domain.academy.service;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

class AcademyServiceTest {
    private AcademyService academyService;
    AcademyRepository academyRepository = Mockito.mock(AcademyRepository.class);
    @InjectMocks
    private Academy academy1;
    private Academy academy2;


    @BeforeEach
    void setUp() {
        academyService = new AcademyService(academyRepository);
        academy1 = Academy.builder().id(1L).name("학원1").businessRegistrationNumber("사업자번호1").build();
        academy2 = Academy.builder().id(2L).name("학원2").businessRegistrationNumber("사업자번호2").build();
    }

    @Nested
    @DisplayName("학원 등록")
    class CreateAcademy {
        CreateAcademyRequest request = new CreateAcademyRequest("학원이름", "학원주소", "010-0000-0000", "원장이름", "0000");

        @Test
        @DisplayName("학원 등록 성공")
        void create_academy_success() {

            given(academyRepository.findByName(anyString())).willReturn(Optional.empty());
            given(academyRepository.findByBusinessRegistrationNumber(anyString())).willReturn(Optional.empty());
            given(academyRepository.save(any(Academy.class))).willReturn(academy1);

            CreateAcademyResponse response = academyService.createAcademy(request);

            assertThat(response.getName()).isEqualTo("학원1");

            then(academyRepository).should(times(1)).findByName(anyString());
            then(academyRepository).should(times(1)).findByBusinessRegistrationNumber(anyString());
            then(academyRepository).should(times(1)).save(any(Academy.class));
        }

        @Test
        @DisplayName("학원 등록 실패 - 학원 이름 중복")
        void create_academy_fail1() {

            given(academyRepository.findByName(anyString())).willReturn(Optional.of(academy1));

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.createAcademy(request));

             assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ACADEMY);

            then(academyRepository).should(times(1)).findByName(anyString());
        }

        @Test
        @DisplayName("학원 등록 실패 - 사업자번호 중복")
         void create_academy_fail2() {

            given(academyRepository.findByName(anyString())).willReturn(Optional.empty());
            given(academyRepository.findByBusinessRegistrationNumber(anyString())).willReturn(Optional.of(academy1));

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.createAcademy(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_BUSINESS_REGISTRATION_NUMBER);

            then(academyRepository).should(times(1)).findByName(anyString());
            then(academyRepository).should(times(1)).findByBusinessRegistrationNumber(anyString());
        }
    }

    @Nested
    @DisplayName("학원 삭제")
    class DeleteAcademy {
        private final Long deleteAcademyId = 1L;

        @Test
        @DisplayName("학원 삭제 성공")
        void delete_academy_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy1));

            Long deletedId = academyService.deleteAcademy(deleteAcademyId);

            assertThat(deletedId).isEqualTo(1L);

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("학원 삭제 실패 - 일치하는 학원 정보 없음")
        void delete_academy_fail() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.deleteAcademy(deleteAcademyId));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);

            then(academyRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("학원 조회")
    class FindAcademy {
        private final FindAcademyRequest request = new FindAcademyRequest("학원1");

        @Test
        @DisplayName("학원 이름으로 학원 조회 성공")
        void find_academy_success() {

            given(academyRepository.findByName(anyString())).willReturn(Optional.of(academy1));

            FindAcademyResponse response = academyService.findAcademy(request);

            assertThat(response.getAcademyId()).isEqualTo(1L);

            then(academyRepository).should(times(1)).findByName(anyString());
        }

        @Test
        @DisplayName("학원 이름으로 학원 조회 실패")
        void find_academy_fail() {

            given(academyRepository.findByName(anyString())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.findAcademy(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);

            then(academyRepository).should(times(1)).findByName(anyString());
        }

        @Test
        @DisplayName("학원 존재 확인")
        void check_academy_success() {

            given(academyRepository.existsByName(anyString())).willReturn(true);

            boolean checkAcademyExist = academyService.checkExistByAcademyName(academy1.getName());

            assertTrue(checkAcademyExist);

            then(academyRepository).should(times(1)).existsByName(anyString());
        }

        @Test
        @DisplayName("학원 id로 학원 조회 성공")
         void  find_acdemyById_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy1));

            FindAcademyResponse response = academyService.findAcademyById(academy1.getId());

            assertThat(response.getAcademyId()).isEqualTo(1L);
            assertThat(response.getAcademyName()).isEqualTo("학원1");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("학원 id로 학원 조회 실패")
        void find_acdemyById_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.findAcademyById(academy1.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);

            then(academyRepository).should(times(1)).findById(anyLong());
        }


        @Test
        @DisplayName("학원 전체 조회 성공")
        void find_All_academy_success() {

            Pageable pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"id");
            Page<Academy> academyList = new PageImpl<>(List.of(academy1, academy2));

            given(academyRepository.findAll(pageable)).willReturn(academyList);

            Page<ReadAcademyResponse> responses = academyService.readAllAcademies(pageable);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findAll(pageable);
        }
    }
}