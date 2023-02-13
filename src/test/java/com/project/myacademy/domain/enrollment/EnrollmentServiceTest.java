package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.BaseEntity;
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
import com.project.myacademy.domain.email.EmailService;
import groovy.util.logging.Slf4j;
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
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
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
    @Mock
    private EmailService emailService;
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
    private Waitinglist waitinglist;
    private Employee mockEmployee;
    private Lecture mockLecture;

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).maximumCapacity(10).currentEnrollmentNumber(0).build();
        student = Student.builder().id(1L).name("student").academyId(academy.getId()).email("email").build();
        student2 = Student.builder().id(2L).name("student2").academyId(academy.getId()).email("email2").build();
        enrollment = Enrollment.builder().id(1L).student(student).lecture(lecture).paymentYN(true).build();
        enrollment2 = Enrollment.builder().id(2L).student(student2).lecture(lecture).paymentYN(true).build();
        waitinglist = Waitinglist.builder().student(student2).lecture(lecture).build();
        mockEmployee = mock(Employee.class);
        mockLecture = mock(Lecture.class);
    }

    @Nested
    @DisplayName("조회")
    class EnrollmentRead{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("수강 리스트 조회 성공")
        void readAllEnrollments_success() {

            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment, enrollment2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findAll(pageable)).willReturn(enrollmentList);

            Page<ReadAllEnrollmentResponse> responseEnrollments = enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable);

            assertThat(responseEnrollments.getTotalPages()).isEqualTo(1);
            assertThat(responseEnrollments.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findAll(pageable);
        }

        @Test
        @DisplayName("수강 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        void readAllEnrollments_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            assertThatThrownBy(() -> enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        void readAllEnrollments_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.readAllEnrollments(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }
    }

    @Nested
    @DisplayName("등록")
    class EnrollmentCreate{

        @Test
        @DisplayName("등록 성공")
        void createEnrollment_success() throws MessagingException {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            when(enrollmentRepository.countByLecture_Id(anyLong())).then(AdditionalAnswers.returnsFirstArg());
            given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment);
//            willDoNothing().given(emailService).sendEmail(anyString(), anyString(), anyString());

            CreateEnrollmentResponse savedEnrollment = enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount());
            assertThat(savedEnrollment.getEnrollmentId()).isEqualTo(1L);
            assertThat(savedEnrollment.getMessage()).isEqualTo("수강 등록 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(enrollmentRepository).should(times(1)).countByLecture_Id(anyLong());
            then(enrollmentRepository).should(times(1)).save(any(Enrollment.class));
//            then(emailService).should(times(1)).sendEmail(anyString(),anyString(),anyString());
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        void createEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        void createEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 수강에 등록될 학생이 존재하지 않을 때")
        void createEnrollment_fail3() {

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
        void createEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(5) - 직원이 수강을 개설할 권한이 아닐 때")
        void createEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("특정 권한의 회원만 접근할 수 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(6) - 수강이 중복 등록되어 있는 경우")
        void createEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.of(enrollment));

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.createEnrollment(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ENROLLMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 중복된 수강신청 입니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }

        @Test
        @DisplayName("등록 실패(7) - 수강 정원 초과")
        void createEnrollment_fail7() {

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
        void updateEnrollment_success() {

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
        void updateEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(2) - 수정 진행하는 직원이 해당 학원 소속이 아닐 때")
        void updateEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("수정 실패(3) - 수정할 수강에 학생이 존재하지 않을 때")
        void updateEnrollment_fail3() {

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
        @DisplayName("수정 실패(4) - 수정할 수강에 강좌가 존재하지 않을 때")
        void updateEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수정 실패(5) - 수강 이력이 존재하지 않을 때")
        void updateEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강신청 내역을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());

        }

        @Test
        @DisplayName("수정 실패(6) - 직원이 수강을 수정할 권한이 아닐 때")
        void updateEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.updateEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), updateEnrollmentRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("특정 권한의 회원만 접근할 수 있습니다.");

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
        void deleteEnrollment_success() throws MessagingException {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
//            willDoNothing().given(emailService).sendEmail(anyString(), anyString(), anyString());

            given(waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(any(Lecture.class))).willReturn(Optional.of(waitinglist));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student2));
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class),any(Lecture.class))).willReturn(Optional.empty());
            given(enrollmentRepository.save(any(Enrollment.class))).willReturn(enrollment2);

            DeleteEnrollmentResponse deletedEnrollment = enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount());
            assertThat(deletedEnrollment.getDeletedEnrollmentId()).isEqualTo(1L);
            assertThat(deletedEnrollment.getMessage()).isEqualTo("수강 등록 삭제 완료");

            then(academyRepository).should(times(2)).findById(anyLong());
            then(employeeRepository).should(times(2)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(2)).findById(anyLong());
            then(lectureRepository).should(times(2)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(enrollmentRepository).should(times(1)).save(any(Enrollment.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
//            then(emailService).should(times(1)).sendEmail(anyString(),anyString(),anyString());
            then(waitinglistRepository).should(times(1)).findTopByLectureOrderByCreatedAtAsc(any(Lecture.class));
        }

        @Test
        @DisplayName("삭제 실패(1) - 학원이 존재하지 않을 때")
        void deleteEnrollment_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(2) - 삭제 진행하는 직원이 해당 학원 소속이 아닐 때")
        void deleteEnrollment_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("삭제 실패(3) - 삭제할 수강에 학생이 존재하지 않을 때")
        void deleteEnrollment_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(4) - 삭제할 수강에 강좌가 존재하지 않을 때")
        void deleteEnrollment_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(5) - 수강 이력이 존재하지 않을 때")
        void deleteEnrollment_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강신청 내역을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(6) - 직원이 수강을 삭제할 권한이 아닐 때")
        void deleteEnrollment_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("특정 권한의 회원만 접근할 수 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("삭제 실패(7) - 대기번호를 수강등록으로 변경하려고 할 때 이미 해당 수강이력이 있는 경우")
        void deleteEnrollment_fail8() throws MessagingException {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
//            willDoNothing().given(emailService).sendEmail(anyString(), anyString(), anyString());

            given(waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(any(Lecture.class))).willReturn(Optional.of(waitinglist));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student2));
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class),any(Lecture.class))).willReturn(Optional.of(enrollment2));

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.deleteEnrollment(academy.getId(), student.getId(), lecture.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ENROLLMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 중복된 수강신청 입니다.");

            then(academyRepository).should(times(2)).findById(anyLong());
            then(employeeRepository).should(times(2)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(2)).findById(anyLong());
            then(lectureRepository).should(times(2)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
//            then(emailService).should(times(1)).sendEmail(anyString(),anyString(),anyString());
            then(waitinglistRepository).should(times(1)).findTopByLectureOrderByCreatedAtAsc(any(Lecture.class));
        }
    }

    @Nested
    @DisplayName("UI")
    class UI{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("결제를 위한 수강 조회 성공")
        void findEnrollment_ForPay() {

            Student student3 = Student.builder().id(3L).name("student").academyId(academy.getId()).build();
            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).employee(teacher).maximumCapacity(10).currentEnrollmentNumber(0).build();
            Enrollment enrollment3 = Enrollment.builder().id(3L).student(student).lecture(lecture2).paymentYN(false).build();
            Enrollment enrollment4 = Enrollment.builder().id(4L).student(student3).lecture(lecture).paymentYN(false).build();
            Enrollment enrollment5 = Enrollment.builder().id(4L).student(student3).lecture(lecture2).paymentYN(false).build();
            ReflectionTestUtils.setField(enrollment, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(enrollment3, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(enrollment4, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(enrollment5, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);

            List<Student> students = new ArrayList<>();
            students.add(student);
            students.add(student3);

            List<Enrollment> studentEnrollments = new ArrayList<>();
            studentEnrollments.add(enrollment);
            studentEnrollments.add(enrollment3);

            List<Enrollment> student3Enrollments = new ArrayList<>();
            student3Enrollments.add(enrollment4);
            student3Enrollments.add(enrollment5);

            PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

            given(studentRepository.findByAcademyIdAndName(academy.getId(), student.getName(), pageable)).willReturn(new PageImpl<>(students));
            given(enrollmentRepository.findByStudentOrderByCreatedAtDesc(student)).willReturn(studentEnrollments);
            given(enrollmentRepository.findByStudentOrderByCreatedAtDesc(student3)).willReturn(student3Enrollments);

            Page<FindEnrollmentResponse> enrollmentForPay = enrollmentService.findEnrollmentForPay(academy.getId(), student.getName(), pageable);

            assertThat(enrollmentForPay.getTotalPages()).isEqualTo(1);
            assertThat(enrollmentForPay.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("특정 학생의 결제가 완료된 수강 신청 내역 조회 성공")
        void findEnrollment_ByStudentId_success() {

            Enrollment enrollment3 = Enrollment.builder().id(3L).student(student).lecture(lecture).paymentYN(true).build();
            ReflectionTestUtils.setField(enrollment, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(enrollment3, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment, enrollment3));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(enrollmentRepository.findByStudentAndPaymentYNIsTrue(student, pageable)).willReturn(enrollmentList);

            Page<FindEnrollmentResponse> response = enrollmentService.findEnrollmentByStudentId(academy.getId(), student.getId(), pageable);
            assertThat(response.getTotalPages()).isEqualTo(1L);
            assertThat(response.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(studentRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByStudentAndPaymentYNIsTrue(student, pageable);
        }

        @Test
        @DisplayName("특정 학생의 결제가 완료된 수강 신청 내역 조회 실패(1) - 학원이 존재하지 않을 때")
        void findEnrollment_ByStudentId_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findEnrollmentByStudentId(academy.getId(), student.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("특정 학생의 결제가 완료된 수강 신청 내역 조회 실패(2) - 학생이 존재하지 않을 때")
        void findEnrollment_ByStudentId_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findEnrollmentByStudentId(academy.getId(), student.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("해당 학원의 미결제 상태의 모든 수강신청내역 조회 성공")
        void findAllEnrollment_ForPay_success() {

            Lecture lecture2 = Lecture.builder().id(2L).name("lecture2").price(10000).employee(teacher).maximumCapacity(10).currentEnrollmentNumber(0).build();
            Enrollment enrollment3 = Enrollment.builder().id(3L).student(student).lecture(lecture2).paymentYN(false).build();
            Enrollment enrollment4 = Enrollment.builder().id(4L).student(student2).lecture(lecture2).paymentYN(false).build();
            ReflectionTestUtils.setField(enrollment3, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
            ReflectionTestUtils.setField(enrollment4, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment3, enrollment4));

            given(enrollmentRepository.findAllByAcademyIdAndPaymentYNIsFalseOrderByCreatedAtDesc(academy.getId(), pageable)).willReturn(enrollmentList);

            Page<FindEnrollmentResponse> response = enrollmentService.findAllEnrollmentForPay(academy.getId(), pageable);
            assertThat(response.getTotalPages()).isEqualTo(1L);
            assertThat(response.getTotalElements()).isEqualTo(2);

            then(enrollmentRepository).should(times(1)).findAllByAcademyIdAndPaymentYNIsFalseOrderByCreatedAtDesc(academy.getId(), pageable);
        }

        @Test
        @DisplayName("해당 학원의 특정 학생의 결제가 끝난 수강 내역 조회 성공")
        void findEnrollment_ForPaySuccess_success() {

            ReflectionTestUtils.setField(enrollment, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);

            given(enrollmentRepository.findByLecture_IdAndStudent_Id(anyLong(), anyLong())).willReturn(Optional.of(enrollment));

            FindEnrollmentResponse response = enrollmentService.findEnrollmentForPaySuccess(student.getId(), lecture.getId());
            assertThat(response.getEnrollmentId()).isEqualTo(1L);
            assertThat(response.getStudentId()).isEqualTo(1L);
            assertThat(response.getStudentName()).isEqualTo("student");
            assertThat(response.getLectureId()).isEqualTo(1L);
            assertThat(response.getLectureName()).isEqualTo("lecture");

            then(enrollmentRepository).should(times(1)).findByLecture_IdAndStudent_Id(anyLong(), anyLong());
        }

        @Test
        @DisplayName("해당 학원의 특정 학생의 수강 내역 조회 실패 - 수강 내역 없음")
        void findEnrollment_ForPaySuccess_fai11() {

            given(enrollmentRepository.findByLecture_IdAndStudent_Id(anyLong(), anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findEnrollmentForPaySuccess(student.getId(), lecture.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강신청 내역을 찾을 수 없습니다.");

            then(enrollmentRepository).should(times(1)).findByLecture_IdAndStudent_Id(anyLong(), anyLong());
        }

        @Test
        @DisplayName("결제 완료된 수강신청내역을 활용해서 출석부에 표시하기 위한 메서드 - 성공")
        void findStudentInfo_FromEnrollment_ByLecture_success() {

            PageImpl<Enrollment> enrollmentList = new PageImpl<>(List.of(enrollment, enrollment2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findByLectureAndPaymentYNIsTrue(lecture, pageable)).willReturn(enrollmentList);

            Page<FindStudentInfoFromEnrollmentByLectureResponse> responses = enrollmentService.findStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId(), pageable);
            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByLectureAndPaymentYNIsTrue(lecture, pageable);
        }

        @Test
        @DisplayName("결제 완료된 수강신청내역을 활용해서 출석부에 표시하기 위한 메서드 - 실패(1) - 학원이 존재하지 않을 때")
        void findStudentInfo_FromEnrollment_ByLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("결제 완료된 수강신청내역을 활용해서 출석부에 표시하기 위한 메서드 - 실패(2) - 직원이 해당 학원 소속이 아닐 때")
        void findStudentInfo_FromEnrollment_ByLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("결제 완료된 수강신청내역을 활용해서 출석부에 표시하기 위한 메서드 - 실패(3) - 강좌가 존재하지 않을 때")
        void findStudentInfo_FromEnrollment_ByLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("결제 완료 여부와 상관없이 수강신청내역을 활용해서 수강 신청자 명단 조회 - 성공")
        void findAllStudentInfo_FromEnrollment_ByLecture_success() {

            Student student3 = Student.builder().id(3L).name("student3").academyId(academy.getId()).build();
            Student student4 = Student.builder().id(4L).name("student4").academyId(academy.getId()).build();
            Enrollment enrollment3 = Enrollment.builder().id(3L).student(student3).lecture(lecture).paymentYN(false).build();
            Enrollment enrollment4 = Enrollment.builder().id(4L).student(student4).lecture(lecture).paymentYN(false).build();

            List<Enrollment> enrollmentList = List.of(enrollment, enrollment2,enrollment3,enrollment4);

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(enrollmentRepository.findByLecture(any(Lecture.class))).willReturn(enrollmentList);

            List<FindStudentInfoFromEnrollmentByLectureResponse> responses = enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId());
            assertThat(responses).hasSize(4);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(enrollmentRepository).should(times(1)).findByLecture(any(Lecture.class));
        }

        @Test
        @DisplayName("결제 완료 여부와 상관없이 수강신청내역을 활용해서 수강 신청자 명단 조회 - 실패(1) - 학원이 존재하지 않을 때")
        void findAllStudentInfo_FromEnrollment_ByLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("결제 완료 여부와 상관없이 수강신청내역을 활용해서 수강 신청자 명단 조회 - 실패(2) - 직원이 해당 학원 소속이 아닐 때")
        void findAllStudentInfo_FromEnrollment_ByLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("결제 완료 여부와 상관없이 수강신청내역을 활용해서 수강 신청자 명단 조회 - 실패(3) - 강좌가 존재하지 않을 때")
        void findAllStudentInfo_FromEnrollment_ByLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academy.getId(), employee.getAccount(), lecture.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }
    }
}