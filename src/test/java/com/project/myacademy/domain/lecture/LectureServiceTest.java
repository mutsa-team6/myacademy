package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.lecture.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AcademyRepository academyRepository;
    @InjectMocks
    private LectureService lectureService;

    private Academy academy;
    private Employee employee;
    private Employee teacher;
    private Lecture lecture;
    private Employee mockEmployee;
    private Employee mockTeacher;
    private Lecture mockLecture1;
    private Lecture mockLecture2;

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).lectureDay("???").finishDate(LocalDate.now().plusDays(3)).employee(teacher).build();
        mockEmployee = mock(Employee.class);
        mockTeacher = mock(Employee.class);
        mockLecture1 = mock(Lecture.class);
        mockLecture2 = mock(Lecture.class);
    }

    @Nested
    @DisplayName("??????")
    class LectureRead {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("?????? ????????? ?????? ??????")
        void readAllLectures_success() {

            Employee teacher2 = Employee.builder().id(3L).name("teacher2").email("email2").account("account2").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).employee(teacher2).build();
            PageImpl<Lecture> lectureList = new PageImpl<>(List.of(lecture, lecture2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findAll(pageable)).willReturn(lectureList);

            Page<ReadAllLectureResponse> responseLectures = lectureService.readAllLectures(academy.getId(), employee.getAccount(), pageable);

            assertThat(responseLectures.getTotalPages()).isEqualTo(1);
            assertThat(responseLectures.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("?????? ????????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
        void readAllLectures_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLectures(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ????????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void readAllLectures_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLectures(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("????????? ?????? ????????? ?????? ??????")
        void readAllLectures_ByTeacher_success() {

            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).employee(teacher).build();
            Lecture lecture3 = Lecture.builder().id(3L).name("lecture3").price(10000).employee(teacher).build();

            PageImpl<Lecture> lectureList = new PageImpl<>(List.of(lecture, lecture2, lecture3));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(employeeRepository.findByIdAndAcademy(anyLong(), any(Academy.class))).willReturn(Optional.of(teacher));
            given(lectureRepository.findByEmployeeAndFinishDateGreaterThanOrderByStartDate(teacher, LocalDate.now(), pageable)).willReturn(lectureList);

            Page<ReadAllLectureResponse> responsePage = lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable);

            assertThat(responsePage.getTotalPages()).isEqualTo(1);
            assertThat(responsePage.getTotalElements()).isEqualTo(3);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(employeeRepository).should(times(1)).findByIdAndAcademy(anyLong(), any(Academy.class));
            then(lectureRepository).should(times(1)).findByEmployeeAndFinishDateGreaterThanOrderByStartDate(teacher, LocalDate.now(), pageable);
        }

        @Test
        @DisplayName("????????? ?????? ????????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
        void readAllLectures_ByTeacher_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("????????? ?????? ????????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void readAllLectures_ByTeacher_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("????????? ?????? ????????? ?????? ??????(3) - ????????? ?????? ?????? ????????? ?????? ???")
        void readAllLectures_ByTeacher_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(employeeRepository.findByIdAndAcademy(anyLong(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(employeeRepository).should(times(1)).findByIdAndAcademy(anyLong(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ????????? ????????? ?????? ????????? ?????? ??????")
        void readAllLectures_ForEnrollment_success() {

            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).employee(teacher).finishDate(LocalDate.now().plusDays(10)).build();
            Lecture lecture3 = Lecture.builder().id(3L).name("lecture3").price(10000).employee(teacher).finishDate(LocalDate.now().plusDays(10)).build();

            PageImpl<Lecture> lectureList = new PageImpl<>(List.of(lecture2, lecture3));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findByAcademyIdAndFinishDateGreaterThanOrderByCreatedAtDesc(academy.getId(), LocalDate.now(), pageable)).willReturn(lectureList);

            Page<ReadAllLectureResponse> responsePage = lectureService.readAllLecturesForEnrollment(academy.getId(), employee.getAccount(), pageable);

            assertThat(responsePage.getTotalPages()).isEqualTo(1);
            assertThat(responsePage.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findByAcademyIdAndFinishDateGreaterThanOrderByCreatedAtDesc(academy.getId(), LocalDate.now(), pageable);
        }

        @Test
        @DisplayName("?????? ????????? ????????? ?????? ????????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
        void readAllLectures_ForEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesForEnrollment(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ????????? ????????? ?????? ????????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void readAllLectures_ForEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesForEnrollment(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

//        @Test
//        @DisplayName("?????? ?????? ?????? ?????? ??????")
//        void readAllTodayLectures_success() {
//
//            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).LectureDay("???").finishDate(LocalDate.now().plusDays(3)).employee(teacher).build();
//            Page<Lecture> lectures = new PageImpl<>(List.of(lecture,lecture2));
////            Page<Lecture> lectures = new PageImpl<>(List.of(mockLecture1,mockLecture2));
//
////            ReflectionTestUtils.setField(lecture, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
////            ReflectionTestUtils.setField(lecture2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
//            ReflectionTestUtils.setField(mockLecture1, Lecture.class, "academyId", 1L, Long.class);
//            ReflectionTestUtils.setField(mockLecture1, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
//            ReflectionTestUtils.setField(mockLecture1, Lecture.class, "LectureDay", "???",String.class);
//            ReflectionTestUtils.setField(mockLecture1, Lecture.class, "finishDate", LocalDate.now().plusDays(3),LocalDate.class);
//
//            ReflectionTestUtils.setField(mockLecture2, Lecture.class, "academyId", 1L, Long.class);
//            ReflectionTestUtils.setField(mockLecture2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
//            ReflectionTestUtils.setField(mockLecture1, Lecture.class, "LectureDay", "???",String.class);
//            ReflectionTestUtils.setField(mockLecture2, Lecture.class, "finishDate", LocalDate.now().plusDays(3),LocalDate.class);
//
//            LocalDate today = LocalDate.now();
//            DayOfWeek dayOfWeek = today.getDayOfWeek();
//            String koreanDay = dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.KOREA);
//
//            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
//            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
//            given(lectureRepository.findByAcademyIdAndFinishDateGreaterThanOrderByCreatedAtDesc(academy.getId(), LocalDate.now(), pageable))
//                    .willReturn(new PageImpl<>(List.of(mockLecture1, mockLecture2)));
//
//            given(mockLecture1.getLectureDay().length()).willReturn(1);
//            given(mockLecture1.getLectureDay()).willReturn(koreanDay);
//            given(mockLecture2.getLectureDay().length()).willReturn(1);
//            given(mockLecture2.getLectureDay()).willReturn(koreanDay);
//
//
//            List<ReadAllLectureResponse> response = lectureService.readAllTodayLectures(academy.getId(), employee.getAccount(), pageable);
//
//            assertThat(response.size()).isEqualTo(2);
//        }
//
//        @Test
//        @DisplayName("?????? ?????? ?????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
//        void readAllTodayLectures_fail1() {
//
//        }
//
//        @Test
//        @DisplayName("?????? ?????? ?????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
//        void readAllTodayLectures_fail2() {
//
//        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class LectureCreate {

        CreateLectureRequest createLectureRequest = CreateLectureRequest.builder().lectureName("lecture").lecturePrice(10000).build();

        @Test
        @DisplayName("?????? ??????")
        void createLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(teacher));
            given(lectureRepository.save(any(Lecture.class))).willReturn(lecture);

            CreateLectureResponse savedLecture = lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount());
            assertThat(savedLecture.getLectureId()).isEqualTo(1L);
            assertThat(savedLecture.getMessage()).isEqualTo("?????? ?????? ??????");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).save(any(Lecture.class));
        }

        @Test
        @DisplayName("?????? ??????(1) - ????????? ???????????? ?????? ???")
        void createLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void createLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ??????(3) - ????????? ????????? ????????? ????????? ?????? ???")
        void createLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("?????? ??????(4) - ????????? ????????? ????????? ???????????? ?????? ???")
        void createLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.TEACHER_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(5) - ????????? ????????? ????????? ????????? STAFF??? ??????")
        void createLecture_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(mockTeacher));
            given(mockTeacher.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(mockTeacher).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("?????? ??????(6) - ????????? ?????? ???????????? ?????? ??????")
        void createLecture_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(mockTeacher));
            given(mockTeacher.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);
            given(lectureRepository.findByName(anyString())).willReturn(Optional.of(lecture));

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_LECTURE);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ?????? ???????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(mockTeacher).should(times(1)).getEmployeeRole();
            then(lectureRepository).should(times(1)).findByName(anyString());
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class LectureUpdate {

        UpdateLectureRequest updateLectureRequest = UpdateLectureRequest.builder().lectureName("lecture").lecturePrice(10000).build();

        @Test
        @DisplayName("?????? ??????")
        void updateLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            UpdateLectureResponse updatedLecture = lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount());
            assertThat(updatedLecture.getLectureId()).isEqualTo(1L);
            assertThat(updatedLecture.getMessage()).isEqualTo("?????? ?????? ?????? ??????");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();

        }

        @Test
        @DisplayName("?????? ??????(1) - ????????? ???????????? ?????? ???")
        void updateLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void updateLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ??????(3) - ????????? ????????? ???????????? ?????? ???")
        void updateLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(4) - ????????? ????????? ????????? ????????? ?????? ???")
        void updateLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class LectureDelete {

        @Test
        @DisplayName("?????? ??????")
        void deleteLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            DeleteLectureResponse deletedLecture = lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount());
            assertThat(deletedLecture.getLectureId()).isEqualTo(1L);
            assertThat(deletedLecture.getMessage()).isEqualTo("?????? ?????? ?????? ??????");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();

        }

        @Test
        @DisplayName("?????? ??????(1) - ????????? ???????????? ?????? ??? ")
        void deleteLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void deleteLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ??????(3) - ????????? ????????? ???????????? ?????? ???")
        void deleteLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(4) - ????????? ????????? ????????? ????????? ?????? ???")
        void deleteLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }
}