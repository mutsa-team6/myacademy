package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.announcement.dto.CreateAnnouncementRequest;
import com.project.myacademy.domain.announcement.dto.CreateAnnouncementResponse;
import com.project.myacademy.domain.announcement.dto.ReadAllAnnouncementResponse;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


class AnnouncementServiceTest {


    EmployeeRepository employeeRepository = Mockito.mock(EmployeeRepository.class);
    AnnouncementRepository announcementRepository = Mockito.mock(AnnouncementRepository.class);
    AcademyRepository academyRepository = Mockito.mock(AcademyRepository.class);
    @InjectMocks
    private AnnouncementService announcementService;
    private Academy academy;
    private Employee employeeSTAFF, employeeUSER;
    private Announcement announcement1, announcement2;
    private Pageable pageable;


    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(employeeRepository, announcementRepository, academyRepository);
        academy = Academy.builder().id(1L).name("학원").build();
        employeeSTAFF = Employee.builder().id(1L).name("직원").account("employeeSTAFF@gmail.com").employeeRole(EmployeeRole.ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(2L).name("강사").account("employeeUSER@gmail.com").employeeRole(EmployeeRole.ROLE_USER).build();
        announcement1 = Announcement.builder().id(1L).academy(academy).title("제목1").body("내용1").build();
        announcement2 = Announcement.builder().id(2L).academy(academy).title("제목2").body("내용2").build();
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
    }

    @Nested
    @DisplayName("공지사항 등록")
    class CreateAnnouncement {

        CreateAnnouncementRequest request = new CreateAnnouncementRequest("제목", "내용");
        @Test
        @DisplayName("공지사항 등록 성공")
        void create_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.save(any())).willReturn(announcement1);

            CreateAnnouncementResponse response = announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request);

            assertThat(response.getTitle().equals("제목1"));
            assertThat(response.getBody().equals("내용1"));
        }

        @Test
        @DisplayName("공지사항 등록 실패1 - 일치하는 학원 정보 없음")
        void create_announcement_fail1() {
            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 등록 실패2 - 일치하는 직원 정보 없음")
        void create_announcement_fail2() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 등록 실패3 - 공지사항 작성 권한 없음")
        void create_announcement_fail() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.of(employeeUSER));

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeUSER.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));
        }
    }

    @Nested
    @DisplayName("공지사항 단건 조회")
    class readOneAnnouncement {

        @Test
        @DisplayName("공지사항 단건 조회 성공")
        void read_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(any())).willReturn(Optional.of(announcement1));

            ReadAnnouncementResponse response = announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount());

            assertThat(response.getTitle().equals("제목1"));
            assertThat(response.getBody().equals("내용1"));
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패1 - 일치하는 학원 정보가 없음")
        void read_announcement_fail1() {
            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패2 - 일치하는 직원 정보가 없음")
        void read_announcement_fail2() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패3 - 일치하는 공지사항 정보가 없음")
        void read_announcement_fail3() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("공지사항 전체 조회")
    class readALLAnnouncement {
        Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1,announcement2));

        @Test
        @DisplayName("공지사항 전체 조회 성공")
        void read_all_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(),any())).willReturn(Optional.of(employeeSTAFF));

            given(announcementRepository.findAllByAcademy(academy, pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> responses = announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount());

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }

    }
}