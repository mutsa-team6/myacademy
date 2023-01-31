package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.dto.*;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.waitinglist.Waitinglist;
import com.project.myacademy.domain.waitinglist.WaitinglistRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private WaitinglistRepository waitinglistRepository;
    @InjectMocks
    private EnrollmentService enrollmentService;

    private Academy academy;
    private Employee employee;
    private Employee teacher;
    private Lecture lecture;
    private Student student;
    private Student student2;
    private Enrollment enrollment;
    private Enrollment enrollment2;
    private Employee mockEmployee;
    private Lecture mockLecture;
    private Enrollment mockEnrollment;
    private Waitinglist waitinglist;

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).maximumCapacity(10).currentEnrollmentNumber(0).build();
        student = Student.builder().id(1L).name("student").academyId(academy.getId()).build();
        student2 = Student.builder().id(2L).name("student2").academyId(academy.getId()).build();
        enrollment = Enrollment.builder().id(1L).student(student).lecture(lecture).build();
        enrollment2 = Enrollment.builder().id(2L).student(student2).lecture(lecture).build();
        waitinglist = Waitinglist.builder().student(student2).lecture(lecture).build();
        mockEmployee = mock(Employee.class);
        mockLecture = mock(Lecture.class);
        mockEnrollment = mock(Enrollment.class);
    }

    @Nested
    @DisplayName("조회")
    class EnrollmentRead{

        @Test
        @DisplayName("수강 리스트 조회 성공")
        public void readAllEnrollments_success() {

            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment, enrollment2));
            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findAll(pageable)).willReturn(enrollmentList);

            Page<ReadAllEnrollmentResponse> responseEnrollments = enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable);

            assertThat(responseEnrollments.getTotalPages()).isEqualTo(1);
            assertThat(responseEnrollments.getTotalElements()).isEqualTo(2);

            then(enrollmentRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("수강 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        public void readAllEnrollments_fail1() {

            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void readAllEnrollments_fail2() {

            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }
    }

    @Nested
    @DisplayName("등록")
    class EnrollmentCreate{

        @Test
        @DisplayName("등록 성공")
        public void createEnrollment_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            when(enrollmentRepository.countByLecture_Id(anyLong())).then(AdditionalAnswers.returnsFirstArg());
            given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);

            CreateEnrollmentResponse savedEnrollment = enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount());
            assertThat(savedEnrollment.getEnrollmentIdId()).isEqualTo(1L);
            assertThat(savedEnrollment.getMessage()).isEqualTo("수강 등록 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(enrollmentRepository).should(times(1)).countByLecture_Id(anyLong());
            then(enrollmentRepository).should(times(1)).save(any(Enrollment.class));
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        public void createEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void createEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 수강에 등록될 학생이 존재하지 않을 때")
        public void createEnrollment_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());

        }

        @Test
        @DisplayName("등록 실패(4) - 수강에 등록될 강좌가 존재하지 않을 때")
        public void createEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(5) - 직원이 수강을 개설할 권한이 아닐 때")
        public void createEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(6) - 수강이 중복 등록되어 있는 경우")
        public void createEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.of(enrollment));

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ENROLLMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 존재하는 수강 내역입니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }

        @Test
        @DisplayName("등록 실패(7) - 수강 정원 초과")
        public void createEnrollment_fail7() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            lenient().when(mockLecture.getCurrentEnrollmentNumber()).thenReturn(lecture.getMaximumCapacity());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.OVER_REGISTRATION_NUMBER);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("최대 수강정원을 초과했습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }
    }

    @Nested
    @DisplayName("수정")
    class EnrollmentUpdate{

        UpdateEnrollmentRequest updateEnrollmentRequest = new UpdateEnrollmentRequest("memo");

        @Test
        @DisplayName("수정 성공")
        public void updateEnrollment_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            UpdateEnrollmentResponse updatedEnrollment = enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount());
            assertThat(updatedEnrollment.getEnrollmentId()).isEqualTo(1L);
            assertThat(updatedEnrollment.getMessage()).isEqualTo("수강 수정 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("수정 실패(1) - 학원이 존재하지 않을 때")
        public void updateEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void updateEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("수정 실패(3) - 수강에 등록될 학생이 존재하지 않을 때")
        public void updateEnrollment_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(4) - 수강에 등록될 강좌가 존재하지 않을 때")
        public void updateEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(5) - 수강 이력이 존재하지 않을 때")
        public void updateEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강 이력을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());

        }

        @Test
        @DisplayName("수정 실패(6) - 직원이 수강을 개설할 권한이 아닐 때")
        public void updateEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }

    @Nested
    @DisplayName("삭제")
    class EnrollmentDelete{

        CreateEnrollmentRequest createEnrollmentRequest = new CreateEnrollmentRequest("memo");

        @Test
        @DisplayName("삭제 성공")
        public void deleteEnrollment_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            given(waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(any(Lecture.class))).willReturn(Optional.of(waitinglist));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student2));
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class),any(Lecture.class))).willReturn(Optional.empty());
            given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment2);

            DeleteEnrollmentResponse deletedEnrollment = enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount());
            assertThat(deletedEnrollment.getDeletedEnrollmentId()).isEqualTo(1L);
            assertThat(deletedEnrollment.getNewEnrollmentId()).isEqualTo(2L);
            assertThat(deletedEnrollment.getMessage()).isEqualTo("기존 수강 내역 삭제 성공, 대기번호 -> 수강 내역 이동 성공");

            then(academyRepository).should(times(2)).findById(anyLong());
            then(employeeRepository).should(times(2)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(2)).findById(anyLong());
            then(lectureRepository).should(times(2)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(enrollmentRepository).should(times(1)).save(any(Enrollment.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(waitinglistRepository).should(times(1)).findTopByLectureOrderByCreatedAtAsc(any(Lecture.class));
        }

        @Test
        @DisplayName("삭제 실패(1) - 학원이 존재하지 않을 때")
        public void deleteEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void deleteEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("삭제 실패(3) - 수강에 등록될 학생이 존재하지 않을 때")
        public void deleteEnrollment_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(4) - 수강에 등록될 강좌가 존재하지 않을 때")
        public void deleteEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(5) - 수강 이력이 존재하지 않을 때")
        public void deleteEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강 이력을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(6) - 직원이 수강을 개설할 권한이 아닐 때")
        public void deleteEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("삭제 실패(7) - 해당 강좌의 다음 대기번호가 존재하지 않을 때")
        public void deleteEnrollment_fail7() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(any(Lecture.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.WAITINGLIST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 대기번호를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(waitinglistRepository).should(times(1)).findTopByLectureOrderByCreatedAtAsc(any(Lecture.class));
        }

        @Test
        @DisplayName("삭제 실패(8) - 대기번호를 수강등록으로 변경하려고 할 때 이미 해당 수강이력이 있는 경우")
        public void deleteEnrollment_fail8() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            given(waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(any(Lecture.class))).willReturn(Optional.of(waitinglist));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student2));
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class),any(Lecture.class))).willReturn(Optional.of(enrollment2));

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), createEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ENROLLMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 존재하는 수강 내역입니다.");

            then(academyRepository).should(times(2)).findById(anyLong());
            then(employeeRepository).should(times(2)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(2)).findById(anyLong());
            then(lectureRepository).should(times(2)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(waitinglistRepository).should(times(1)).findTopByLectureOrderByCreatedAtAsc(any(Lecture.class));
        }
    }

    @Nested
    @DisplayName("UI")
    class UI{

//        @Test
//        @DisplayName("결제를 위한 수강 조회")
//        public void findEnrollment_ForPay() {
//
//            List<Student> students = new ArrayList<>();
//            students.add(student);
//            students.add(student2);
//
//            List<Enrollment> enrollments = new ArrayList<>();
//            enrollments.add(enrollment);
//            enrollments.add(enrollment2);
//
//            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment, enrollment2));
//            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");
//
//            given(studentRepository.findByAcademyIdAndName(anyLong(),anyString())).willReturn(students);
//            given(enrollmentRepository.findByStudentOrderByCreatedAtDesc(student)).willReturn(enrollments);
//            given(enrollmentRepository.findByStudentOrderByCreatedAtDesc(student2)).willReturn(enrollments);
//
//            given(enrollmentRepository.findAll(pageable)).willReturn(enrollmentList);
//
//            Page<FindEnrollmentResponse> enrollmentForPay = enrollmentService.findEnrollmentForPay(academy.getId(), student.getName());
//
//            assertThat(enrollmentForPay.getTotalPages()).isEqualTo(1);
//            assertThat(enrollmentForPay.getTotalElements()).isEqualTo(2);
//        }

        @Test
        @DisplayName("해당 학원의 모든 수강내역 조회")
        public void findAllEnrollment_ForPay() {

        }

    }
}