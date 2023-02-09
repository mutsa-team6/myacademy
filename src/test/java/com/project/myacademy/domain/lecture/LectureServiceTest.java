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

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).build();
        mockEmployee = mock(Employee.class);
        mockTeacher = mock(Employee.class);
    }

    @Nested
    @DisplayName("조회")
    class LectureRead {

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("강좌 리스트 조회 성공")
        public void readAllLectures_success() {

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
        @DisplayName("강좌 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        public void readAllLectures_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLectures(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("강좌 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void readAllLectures_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLectures(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강사의 강좌 리스트 조회 성공")
        public void readAllLectures_ByTeacher_success() {

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
        @DisplayName("강사의 강좌 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        public void readAllLectures_ByTeacher_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("강사의 강좌 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void readAllLectures_ByTeacher_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강사의 강좌 리스트 조회 실패(3) - 강사가 해당 학원 소속이 아닐 때")
        public void readAllLectures_ByTeacher_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(employeeRepository.findByIdAndAcademy(anyLong(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesByTeacherId(academy.getId(), employee.getAccount(), teacher.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 직원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(employeeRepository).should(times(1)).findByIdAndAcademy(anyLong(), any(Academy.class));
        }

        @Test
        @DisplayName("수강 목록에 보여질 강좌 리스트 조회 성공")
        public void readAllLectures_ForEnrollment_success() {

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
        @DisplayName("수강 목록에 보여질 강좌 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        public void readAllLectures_ForEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesForEnrollment(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강 목록에 보여질 강좌 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void readAllLectures_ForEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.readAllLecturesForEnrollment(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }
    }

    @Nested
    @DisplayName("강좌 등록")
    class LectureCreate {

        CreateLectureRequest createLectureRequest = CreateLectureRequest.builder().lectureName("lecture").lecturePrice(10000).build();

        @Test
        @DisplayName("등록 성공")
        public void createLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(teacher));
            given(lectureRepository.save(any(Lecture.class))).willReturn(lecture);

            CreateLectureResponse savedLecture = lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount());
            assertThat(savedLecture.getLectureId()).isEqualTo(1L);
            assertThat(savedLecture.getMessage()).isEqualTo("강좌 등록 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).save(any(Lecture.class));
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        public void createLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void createLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 직원이 강좌를 개설할 권한이 아닐 때")
        public void createLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(4) - 강좌에 등록될 강사가 존재하지 않을 때")
        public void createLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.TEACHER_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강사를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(5) - 강좌에 등록될 강사의 권한이 STAFF인 경우")
        public void createLecture_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(mockTeacher));
            given(mockTeacher.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(mockTeacher).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(6) - 강좌가 중복 등록되어 있는 경우")
        public void createLecture_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(employeeRepository.findById(anyLong())).willReturn(Optional.of(mockTeacher));
            given(mockTeacher.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);
            given(lectureRepository.findByName(anyString())).willReturn(Optional.of(lecture));

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.createLecture(academy.getId(), employee.getId(), createLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_LECTURE);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 존재하는 수업입니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(employeeRepository).should(times(1)).findById(anyLong());
            then(mockTeacher).should(times(1)).getEmployeeRole();
            then(lectureRepository).should(times(1)).findByName(anyString());
        }
    }

    @Nested
    @DisplayName("강좌 수정")
    class LectureUpdate {

        UpdateLectureRequest updateLectureRequest = UpdateLectureRequest.builder().lectureName("lecture").lecturePrice(10000).build();

        @Test
        @DisplayName("수정 성공")
        public void updateLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            UpdateLectureResponse updatedLecture = lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount());
            assertThat(updatedLecture.getLectureId()).isEqualTo(1L);
            assertThat(updatedLecture.getMessage()).isEqualTo("강좌 정보 변경 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();

        }

        @Test
        @DisplayName("수정 실패(1) - 학원이 존재하지 않을 때")
        public void updateLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(2) - 수정 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void updateLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("수정 실패(3) - 수정할 강좌가 존재하지 않을 때")
        public void updateLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(4) - 직원이 강좌를 수정할 권한이 아닐 때")
        public void updateLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.updateLecture(academy.getId(), lecture.getId(), updateLectureRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }

    @Nested
    @DisplayName("강좌 삭제")
    class LectureDelete {

        @Test
        @DisplayName("삭제 성공")
        public void deleteLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            DeleteLectureResponse deletedLecture = lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount());
            assertThat(deletedLecture.getLectureId()).isEqualTo(1L);
            assertThat(deletedLecture.getMessage()).isEqualTo("강좌 정보 삭제 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();

        }

        @Test
        @DisplayName("삭제 실패(1) - 학원이 존재하지 않을 때 ")
        public void deleteLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(2) - 삭제 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void deleteLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("삭제 실패(3) - 삭제할 강좌가 존재하지 않을 때")
        public void deleteLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(4) - 직원이 강좌를 삭제할 권한이 아닐 때")
        public void deleteLecture_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> lectureService.deleteLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }
}