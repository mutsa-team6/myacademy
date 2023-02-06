package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.announcement.dto.*;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
    AcademyRepository academyRepository = mock(AcademyRepository.class);
    @InjectMocks
    private AnnouncementService announcementService;
    private Academy academy;
    private Employee employeeADMIN, employeeSTAFF, employeeUSER;
    private Announcement announcement1, announcement2, announcement3;
    private Pageable pageable;

    private Employee mockEmployee;


    @BeforeEach
    void setUp() {
        announcementService = new AnnouncementService(employeeRepository, announcementRepository, academyRepository);
        academy = Academy.builder().id(1L).name("학원").build();
        employeeSTAFF = Employee.builder().id(1L).name("직원").account("employeeSTAFF@gmail.com").employeeRole(EmployeeRole.ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(2L).name("강사").account("employeeUSER@gmail.com").employeeRole(EmployeeRole.ROLE_USER).build();
        employeeADMIN = Employee.builder().id(3L).name("대표").account("employeeADMIN@gmail.com").employeeRole(EmployeeRole.ROLE_ADMIN).build();
        announcement1 = Announcement.builder().id(1L).academy(academy).employee(employeeSTAFF).title("제목1").body("내용1").type(AnnouncementType.ANNOUNCEMENT).build();
        announcement2 = Announcement.builder().id(2L).academy(academy).employee(employeeSTAFF).title("제목2").body("내용2").type(AnnouncementType.ANNOUNCEMENT).build();
        announcement3 = Announcement.builder().id(3L).academy(academy).employee(employeeSTAFF).title("제목3").body("내용3").type(AnnouncementType.ADMISSION).build();
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
        mockEmployee = mock(Employee.class);
    }

    @Nested
    @DisplayName("공지사항 등록")
    class CreateAnnouncement {

        CreateAnnouncementRequest request = new CreateAnnouncementRequest("제목", "내용", AnnouncementType.ADMISSION);

        @Test
        @DisplayName("공지사항 등록 성공")
        void create_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
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
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 등록 실패3 - 공지사항 작성 권한 없음")
        void create_announcement_fail() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

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

            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
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
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패3 - 일치하는 공지사항 정보가 없음")
        void read_announcement_fail3() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("공지사항 전체 조회")
    class readALLAnnouncement {

        @Test
        @DisplayName("공지사항 전체 조회 성공")
        void read_all_announcement_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1, announcement2));

            given(announcementRepository.findAllByAcademyOrderByCreatedAtDesc(academy, pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> responses = announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount());

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("공지사항 전체 조회 실패1 - 일치하는 학원 정보가 없음")
        void read_all_announcement_fail1() {
            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 전체 조회 실패2 - 일치하는 직원 정보가 없음")
        void read_all_announcement_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 타입별 전체 조회 성공")
        void read_type_announcement_success() {

            String stringType = "ANNOUNCEMENT";
            AnnouncementType type = AnnouncementType.ANNOUNCEMENT;
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1, announcement2));

            given(announcementRepository.findAllByTypeAndAcademy(type, academy, pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> responses = announcementService.readTypeAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount(), stringType);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class updateAnnouncement {
        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest("바뀐제목", "바뀐내용");

        @Test
        @DisplayName("공지사항 수정 성공")
        void update_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(any())).willReturn(Optional.of(announcement1));

            UpdateAnnouncementResponse response = announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getTitle().equals("바뀐제목"));
            assertThat(response.getBody().equals("바뀐내용"));
        }

        @Test
        @DisplayName("공지사항 수정 실패1 - 일치하는 학원 정보 없음")
        void update_announcement_fail1() {
            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 수정 실패2 - 일치하는 직원 정보 없음")
        void update_announcement_fail2() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 수정 실패3 - 직원의 권한이 USER인 경우")
        void update_announcement_fail3() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, mockEmployee.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));
        }

        @Test
        @DisplayName("공지사항 수정 실패4 - 일치하는 공지사항 정보 없음")
        void update_announcement_fail4() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(announcementRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, mockEmployee.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class deleteAnnouncement {

        @Test
        @DisplayName("공지사항 삭제 성공")
        void delete_announcement_success() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(any())).willReturn(Optional.of(announcement1));

            DeleteAnnouncementResponse response = announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount());

            assertThat(response.getId().equals(1L));
            assertThat(response.getTitle().equals("제목1"));
        }

        @Test
        @DisplayName("공지사항 삭제 실패1 - 일치하는 학원 정보 없음")
        void delete_announcement_fail1() {
            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 삭제 실패2 - 일치하는 직원 정보 없음")
        void delete_announcement_fail2() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("공지사항 삭제 실패3 - 사용자 권한 없음")
        void delete_announcement_fail3() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), mockEmployee.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));
        }

        @Test
        @DisplayName("공지사항 삭제 실패4 - 일치하는 공지사항 정보 없음")
        void delete_announcement_fail4() {
            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(announcementRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), mockEmployee.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
        }
    }
}