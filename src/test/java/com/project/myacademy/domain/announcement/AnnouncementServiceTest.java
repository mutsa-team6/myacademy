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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private AcademyRepository academyRepository;
    @InjectMocks
    private AnnouncementService announcementService;
    private Academy academy;
    private Employee employeeADMIN, employeeSTAFF, employeeUSER;
    private Announcement announcement1, announcement2, announcement3;
    private Pageable pageable;
    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        academy = Academy.builder().id(1L).name("학원").build();
        employeeSTAFF = Employee.builder().id(1L).name("직원").account("employeeSTAFF@gmail.com").employeeRole(EmployeeRole.ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(2L).name("강사").account("employeeUSER@gmail.com").employeeRole(EmployeeRole.ROLE_USER).build();
        employeeADMIN = Employee.builder().id(3L).name("대표").account("employeeADMIN@gmail.com").employeeRole(EmployeeRole.ROLE_ADMIN).build();
        announcement1 = Announcement.builder().id(1L).academy(academy).employee(employeeSTAFF).title("제목").body("내용").type(AnnouncementType.ANNOUNCEMENT).build();
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

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.save(any(Announcement.class))).willReturn(announcement1);

            CreateAnnouncementResponse response = announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request);

            assertThat(response.getTitle().equals("제목1"));
            assertThat(response.getBody().equals("내용1"));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 등록 실패1 - 일치하는 학원 정보 없음")
        void create_announcement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 등록 실패2 - 일치하는 직원 정보 없음")
        void create_announcement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeSTAFF.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 등록 실패3 - 공지사항 작성 권한 없음")
        void create_announcement_fail() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeUSER));

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.createAnnouncement(academy.getId(), employeeUSER.getAccount(), request));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }
    }

    @Nested
    @DisplayName("공지사항 단건 조회")
    class readOneAnnouncement {

        @Test
        @DisplayName("공지사항 단건 조회 성공")
        void read_announcement_success() {

            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(anyLong())).willReturn(Optional.of(announcement1));

            ReadAnnouncementResponse response = announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount());

            assertThat(response.getTitle().equals("제목1"));
            assertThat(response.getBody().equals("내용1"));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패1 - 일치하는 학원 정보가 없음")
        void read_announcement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패2 - 일치하는 직원 정보가 없음")
        void read_announcement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 단건 조회 실패3 - 일치하는 공지사항 정보가 없음")
        void read_announcement_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("공지사항 전체 조회")
    class readALLAnnouncement {

        @Test
        @DisplayName("공지사항 전체 조회 성공")
        void read_all_announcement_success() {

            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1, announcement2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findAllByAcademyOrderByCreatedAtDesc(academy, pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> responses = announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount());

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findAllByAcademyOrderByCreatedAtDesc(academy, pageable);
        }

        @Test
        @DisplayName("공지사항 전체 조회 실패1 - 일치하는 학원 정보가 없음")
        void read_all_announcement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 전체 조회 실패2 - 일치하는 직원 정보가 없음")
        void read_all_announcement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAllAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 타입별 전체 조회 성공")
        void read_type_announcement_success() {

            String stringType = "ANNOUNCEMENT";
            AnnouncementType type = AnnouncementType.ANNOUNCEMENT;
            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1, announcement2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findAllByTypeAndAcademy(type, academy, pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> responses = announcementService.readTypeAnnouncement(academy.getId(), pageable, employeeSTAFF.getAccount(), stringType);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findAllByTypeAndAcademy(type, academy, pageable);
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class updateAnnouncement {
        UpdateAnnouncementRequest request = new UpdateAnnouncementRequest("바뀐제목", "바뀐내용");

        @Test
        @DisplayName("공지사항 수정 성공")
        void update_announcement_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(anyLong())).willReturn(Optional.of(announcement1));

            UpdateAnnouncementResponse response = announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getTitle().equals("바뀐제목"));
            assertThat(response.getBody().equals("바뀐내용"));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 수정 실패1 - 일치하는 학원 정보 없음")
        void update_announcement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());

        }

        @Test
        @DisplayName("공지사항 수정 실패2 - 일치하는 직원 정보 없음")
        void update_announcement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 수정 실패3 - 직원의 권한이 USER인 경우")
        void update_announcement_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("공지사항 수정 실패4 - 일치하는 공지사항 정보 없음")
        void update_announcement_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(announcementRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.updateAnnouncement(academy.getId(), announcement1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(announcementRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class deleteAnnouncement {

        @Test
        @DisplayName("공지사항 삭제 성공")
        void delete_announcement_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findById(anyLong())).willReturn(Optional.of(announcement1));

            DeleteAnnouncementResponse response = announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount());

            assertThat(response.getId().equals(1L));
            assertThat(response.getTitle().equals("제목1"));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 삭제 실패1 - 일치하는 학원 정보 없음")
        void delete_announcement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 삭제 실패2 - 일치하는 직원 정보 없음")
        void delete_announcement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 삭제 실패3 - 사용자 권한 없음")
        void delete_announcement_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PERMISSION));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("공지사항 삭제 실패4 - 일치하는 공지사항 정보 없음")
        void delete_announcement_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(announcementRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.deleteAnnouncement(academy.getId(), announcement1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(announcementRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("UI용 메서드")
    class ui{

        @Test
        @DisplayName("메인에 공지사항 5개 보여주기 성공")
        public void readAnnouncement_forMain_success() {

            Announcement announcement4 = Announcement.builder().id(4L).academy(academy).employee(employeeSTAFF).title("제목4").body("내용4").type(AnnouncementType.ANNOUNCEMENT).build();
            Announcement announcement5 = Announcement.builder().id(5L).academy(academy).employee(employeeSTAFF).title("제목5").body("내용5").type(AnnouncementType.ANNOUNCEMENT).build();
            Announcement announcement6 = Announcement.builder().id(6L).academy(academy).employee(employeeSTAFF).title("제목6").body("내용6").type(AnnouncementType.ANNOUNCEMENT).build();

            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 5, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement4, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 7, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement5, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 8, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement6, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 9, 13, 0), LocalDateTime.class);
            List<Announcement> announcementList = List.of(announcement1, announcement2, announcement4, announcement5, announcement6);

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ANNOUNCEMENT, academy)).willReturn(announcementList);

            List<ReadAnnouncementResponse> response = announcementService.readAnnouncementForMain(academy.getId(), employeeSTAFF.getAccount());

            assertThat(response.size()).isEqualTo(5);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ANNOUNCEMENT, academy);
        }

        @Test
        @DisplayName("메인에 공지사항 5개 보여주기 실패(1) - 일치하는 학원 정보 없음")
        public void readAnnouncement_forMain_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncementForMain(academy.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("메인에 공지사항 5개 보여주기 실패(2) - 일치하는 직원 정보 없음")
        public void readAnnouncement_forMain_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAnnouncementForMain(academy.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("메인에 입시정보 5개 보여주기 성공")
        public void readAdmission_forMain_success() {

            Announcement announcement4 = Announcement.builder().id(4L).academy(academy).employee(employeeSTAFF).title("제목4").body("내용4").type(AnnouncementType.ADMISSION).build();
            Announcement announcement5 = Announcement.builder().id(5L).academy(academy).employee(employeeSTAFF).title("제목5").body("내용5").type(AnnouncementType.ADMISSION).build();
            Announcement announcement6 = Announcement.builder().id(6L).academy(academy).employee(employeeSTAFF).title("제목6").body("내용6").type(AnnouncementType.ADMISSION).build();

            ReflectionTestUtils.setField(announcement2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement4, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 7, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement5, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 8, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement6, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 9, 13, 0), LocalDateTime.class);
            List<Announcement> announcementList = List.of(announcement2, announcement4, announcement5, announcement6);

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ADMISSION, academy)).willReturn(announcementList);

            List<ReadAnnouncementResponse> response = announcementService.readAdmissionForMain(academy.getId(), employeeSTAFF.getAccount());

            assertThat(response.size()).isEqualTo(4);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ADMISSION, academy);
        }

        @Test
        @DisplayName("메인에 입시정보 5개 보여주기 실패(1) - 일치하는 학원 정보 없음")
        public void readAdmission_forMain_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAdmissionForMain(academy.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("메인에 입시정보 5개 보여주기 실패(2) - 일치하는 직원 정보 없음")
        public void readAdmission_forMain_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.readAdmissionForMain(academy.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("공지사항 검색 성공")
        public void searchAnnouncement_success() {

            Announcement announcement4 = Announcement.builder().id(4L).academy(academy).employee(employeeSTAFF).title("제목").body("내용4").type(AnnouncementType.ADMISSION).build();
            Announcement announcement5 = Announcement.builder().id(5L).academy(academy).employee(employeeSTAFF).title("제목").body("내용5").type(AnnouncementType.ADMISSION).build();
            Announcement announcement6 = Announcement.builder().id(6L).academy(academy).employee(employeeSTAFF).title("제목").body("내용6").type(AnnouncementType.ADMISSION).build();

            ReflectionTestUtils.setField(announcement1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement4, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 7, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement5, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 8, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(announcement6, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 9, 13, 0), LocalDateTime.class);
            Page<Announcement> announcementList = new PageImpl<>(List.of(announcement1, announcement4, announcement5, announcement6));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employeeSTAFF));
            given(announcementRepository.findAllByAcademyAndTitleContainingOrderByCreatedAtDesc(academy, announcement1.getTitle(), pageable)).willReturn(announcementList);

            Page<ReadAllAnnouncementResponse> response = announcementService.searchAnnouncement(academy.getId(), "제목", pageable, employeeSTAFF.getAccount());
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getTotalElements()).isEqualTo(4);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(announcementRepository).should(times(1)).findAllByAcademyAndTitleContainingOrderByCreatedAtDesc(academy, announcement1.getTitle(), pageable);
        }

        @Test
        @DisplayName("공지사항 검색 실패(1) - 일치하는 학원 정보 없음")
        public void searchAnnouncement_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.searchAnnouncement(academy.getId(), "제목", pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("공지사항 검색 실패(2) - 일치하는 직원 정보 없음")
        public void searchAnnouncement_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> announcementService.searchAnnouncement(academy.getId(), "제목", pageable, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }
    }
}