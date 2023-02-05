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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AcademyServiceTest {
    private AcademyService academyService;
    AcademyRepository academyRepository = Mockito.mock(AcademyRepository.class);
    @InjectMocks
    private Academy academy1;
    private Academy academy2;


    @BeforeEach
    void setUp() {
        academyService = new AcademyService(academyRepository);
        academy1 = Academy.builder().id(1L).name("학원1`").build();
        academy2 = Academy.builder().id(1L).name("학원2`").build();
    }

    @Nested
    @DisplayName("학원 등록")
    class CreateAcademy {
        CreateAcademyRequest request = new CreateAcademyRequest("학원이름", "학원주소", "010-0000-0000", "원장이름", "0000");

        @Test
        @DisplayName("학원 등록 성공")
        void create_academy_success() {

            Academy savedAcademy = Academy.createAcademy(request);
            given(academyRepository.save(any())).willReturn(savedAcademy);

            CreateAcademyResponse response = academyService.createAcademy(request);

            assertEquals("학원이름", response.getName());

        }

        @Test
        @DisplayName("학원 등록 실패 - 학원 이름 중복")
        void create_academy_fail() {

            given(academyRepository.findByName(request.getName())).willReturn(Optional.of(academy1));

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.createAcademy(request));

             assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ACADEMY);
        }
    }

    @Nested
    @DisplayName("학원 삭제")
    class DeleteAcademy {
        private final Long deleteAcademyId = 1L;

        @Test
        @DisplayName("학원 삭제 성공")
        void delete_academy_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy1));

            Long deletedId = academyService.deleteAcademy(deleteAcademyId);

            assertEquals(1, deletedId);
        }

        @Test
        @DisplayName("학원 삭제 실패 - 일치하는 학원 정보 없음")
        void delete_academy_fail() {

            given(academyRepository.findById(deleteAcademyId)).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.deleteAcademy(deleteAcademyId));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("학원 조회")
    class FindAcademy {
        private final FindAcademyRequest request = new FindAcademyRequest("학원1");

        @Test
        @DisplayName("학원 조회 성공")
        void find_academy_success() {

            given(academyRepository.findByName(request.getName())).willReturn(Optional.of(academy1));

            FindAcademyResponse response = academyService.findAcademy(request);

            assertEquals(1L, response.getAcademyId());
        }

        @Test
        @DisplayName("학원 조회 실패")
        void find_academy_fail() {

            given(academyRepository.findByName(request.getName())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> academyService.findAcademy(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학원 존재 확인")
        void check_academy_success() {

            given(academyRepository.existsByName(any())).willReturn(true);

            boolean checkAcademyExist = academyService.checkExistByAcademyName(academy1.getName());

            assertTrue(checkAcademyExist);
        }


        @Test
        @DisplayName("학원 전체 조회 성공")
        void find_All_academy_success() {
        ReadAcademyResponse response1 = new ReadAcademyResponse(academy1);
        ReadAcademyResponse response2 = new ReadAcademyResponse(academy2);

            Pageable pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"id");
            Page<Academy> academyList = new PageImpl<>(List.of(academy1, academy2));

            given(academyRepository.findAll(pageable)).willReturn(academyList);

            Page<ReadAcademyResponse> responses = academyService.readAllAcademies(pageable);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);

        }

    }
}