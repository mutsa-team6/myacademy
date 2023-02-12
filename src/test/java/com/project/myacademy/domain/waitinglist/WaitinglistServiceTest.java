package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.DeleteWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WaitinglistServiceTest {

    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private WaitinglistRepository waitinglistRepository;
    @Mock
    private EmailUtil emailUtil;
    @InjectMocks
    private WaitinglistService waitinglistService;

    private Academy academy;
    private Employee employee;
    private Employee teacher;
    private Lecture lecture;
    private Student student;
    private Student student2;
    @Spy
    private Waitinglist waitinglist;
    @Spy
    private Waitinglist waitinglist2;
    private Enrollment enrollment;
    private Employee mockEmployee;
    private Lecture mockLecture;

    @Spy
    List<Waitinglist> waitinglists = new ArrayList<>();

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).minimumCapacity(5).maximumCapacity(10).build();
        student = Student.builder().id(1L).name("student").academyId(academy.getId()).email("email").build();
        student2 = Student.builder().id(2L).name("student2").academyId(academy.getId()).email("email2").build();
        enrollment = Enrollment.builder().id(1L).student(student).lecture(lecture).build();
        waitinglist = Waitinglist.builder().id(1L).student(student).lecture(lecture).build();
        waitinglist2 = Waitinglist.builder().id(2L).student(student2).lecture(lecture).build();
        mockEmployee = mock(Employee.class);
        mockLecture = mock(Lecture.class);
        waitinglists.add(waitinglist);
        waitinglists.add(waitinglist2);
    }

    @Nested
    @DisplayName("조회")
    class WaitingListRead{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        @Test
        @DisplayName("대기번호 리스트 조회 성공")
        void readAllWaitingList_success() {

            PageImpl<Waitinglist> waitingLists = new PageImpl<>(List.of(waitinglist, waitinglist2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(waitinglistRepository.findAll(pageable)).willReturn(waitingLists);

            Page<ReadAllWaitinglistResponse> responseWaitinglists = waitinglistService.readAllWaitinglists(academy.getId(), employee.getAccount(), pageable);

            assertThat(responseWaitinglists.getTotalPages()).isEqualTo(1);
            assertThat(responseWaitinglists.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(waitinglistRepository).should(times(1)).findAll(pageable);

        }

        @Test
        @DisplayName("대기번호 리스트 조회 실패(1) - 학원이 존재하지 않을 때")
        void readAllWaitingList_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.readAllWaitinglists(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("대기번호 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        void readAllWaitingList_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.readAllWaitinglists(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 성공")
        void countWaitingList_ByLecture_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(waitinglistRepository.countWaitinglistByLecture(mockLecture)).willReturn(anyLong());

            Long counts = waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount());

            assertThat(counts).isNotNull();

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(waitinglistRepository).should(times(1)).countWaitinglistByLecture(mockLecture);
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 실패(1) - 학원이 존재하지 않을 때")
        void countWaitingList_ByLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        void countWaitingList_ByLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 실패(3) - 대기번호 조회 대상인 강좌가 존재하지 않을 때")
        void countWaitingList_ByLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("등록")
    class WaitingListCreate{

        @Test
        @DisplayName("등록 성공")
        void createWaitingList_success() throws MessagingException {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(mockLecture.getCurrentEnrollmentNumber()).willReturn(lecture.getMaximumCapacity());
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            given(waitinglistRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            given(waitinglistRepository.saveAndFlush(any(Waitinglist.class))).willReturn(waitinglist);
            willDoNothing().given(emailUtil).sendEmail(anyString(), anyString(), anyString());

            CreateWaitinglistResponse savedWaitingList = waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount());
            assertThat(savedWaitingList.getWaitinglistId()).isEqualTo(1L);
            assertThat(savedWaitingList.getMessage()).isEqualTo("대기번호 등록 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(mockLecture).should(times(1)).getCurrentEnrollmentNumber();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(waitinglistRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(waitinglistRepository).should(times(1)).saveAndFlush(any(Waitinglist.class));
            then(emailUtil).should(times(1)).sendEmail(anyString(),anyString(),anyString());
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        void createWaitingList_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        void createWaitingList_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 대기번호에 등록될 학생이 존재하지 않을 때")
        void createWaitingList_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(4) - 대기번호에 등록될 강좌가 존재하지 않을 때")
        void createWaitingList_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(5) - 직원이 대기번호를 등록할 권한이 아닐 때")
        void createWaitingList_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("특정 권한의 회원만 접근할 수 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

//        @Test
//        @DisplayName("등록 실패(6) - 최대 수강 정원보다 적어서 수강 등록으로 진행해야 할 때")
//        void createWaitingList_fail6() {
//
////            ReflectionTestUtils.setField(mockLecture, Lecture.class, "currentEnrollmentNumber", 15, Integer.class);
////            ReflectionTestUtils.setField(lecture, Lecture.class, "maximumCapacity", 10, Integer.class);
//
//            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
//            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
//            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
//            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
//            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
//            given(mockLecture.getCurrentEnrollmentNumber()).willReturn(lecture.getMinimumCapacity());
////            given(mockLecture.getCurrentEnrollmentNumber()).will(invocation -> {
////                mockLecture = invocation.getArgument(0);
////                return lecture.getMaximumCapacity();
////                    });
////            given(mockLecture.getCurrentEnrollmentNumber()).will((InvocationOnMock invocation) ->
////                    invocation.getMethod().equals(lecture.getMaximumCapacity()));
//
//            AppException appException = assertThrows(AppException.class,
//                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));
//
//            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.CANNOT_REGISTER_WAITINGLIST);
//            assertThat(appException.getErrorCode().getMessage()).isEqualTo("아직 수강 정원이 다 차지 않아 수강 등록으로 진행해야 합니다.");
//
//            then(academyRepository).should(times(1)).findById(anyLong());
//            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
//            then(studentRepository).should(times(1)).findById(anyLong());
//            then(lectureRepository).should(times(1)).findById(anyLong());
//            then(mockEmployee).should(times(1)).getEmployeeRole();
////            then(mockLecture).should(times(1)).getCurrentEnrollmentNumber();
//        }

        @Test
        @DisplayName("등록 실패(7) - 이미 수강 등록 되어 있는 경우")
        void createWaitingList_fail7() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(mockLecture.getCurrentEnrollmentNumber()).willReturn(lecture.getMinimumCapacity());
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.of(enrollment));

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ENROLLMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 중복된 수강신청 입니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(mockLecture).should(times(1)).getCurrentEnrollmentNumber();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }

        @Test
        @DisplayName("등록 실패(8) - 대기번호가 중복 등록되어 있는 경우")
        void createWaitingList_fail8() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(mockLecture.getCurrentEnrollmentNumber()).willReturn(lecture.getMinimumCapacity());
            given(enrollmentRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            given(waitinglistRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.of(waitinglist));

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_WAITINGLIST);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 대기번호에 등록되어 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(mockLecture).should(times(1)).getCurrentEnrollmentNumber();
            then(enrollmentRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(waitinglistRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }
    }

    @Nested
    @DisplayName("삭제")
    class WaitinglistDelete {

        @Test
        @DisplayName("삭제 성공")
        void deleteWaitinglist_success() throws MessagingException {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(waitinglistRepository.findById(anyLong())).willReturn(Optional.of(waitinglist));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            willDoNothing().given(emailUtil).sendEmail(anyString(), anyString(), anyString());

            DeleteWaitinglistResponse deletedWaitinglist = waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount());
            assertThat(deletedWaitinglist.getWaitinglistId()).isEqualTo(1L);
            assertThat(deletedWaitinglist.getMessage()).isEqualTo("대기번호 삭제 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(waitinglistRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(emailUtil).should(times(1)).sendEmail(anyString(),anyString(),anyString());
        }

        @Test
        @DisplayName("삭제 실패(1) - 학원이 존재하지 않을 때")
        void deleteWaitinglist_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(2) - 삭제 진행하는 직원이 해당 학원 소속이 아닐 때")
        void deleteWaitinglist_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("삭제 실패(3) - 학생이 존재하지 않을 때")
        void deleteWaitinglist_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학생을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(4) - 강좌가 존재하지 않을 때")
        void deleteWaitinglist_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(5) - 삭제할 대기번호가 존재하지 않을 때")
        void deleteWaitinglist_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(waitinglistRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.WAITINGLIST_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강신청 대기 번호를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(waitinglistRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(6) - 직원이 대기번호를 삭제할 권한이 아닐 때")
        void deleteWaitinglist_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(mockLecture));
            given(waitinglistRepository.findById(anyLong())).willReturn(Optional.of(waitinglist));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.deleteWaitinglist(academy.getId(), student.getId(), lecture.getId(), waitinglist.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("특정 권한의 회원만 접근할 수 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(waitinglistRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }

    @Nested
    @DisplayName("UI용 메서드")
    class UI {

//        @Test
//        @DisplayName("성공")
//        void ui_success() {
//
//            ReflectionTestUtils.setField(waitinglist, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 12, 0), LocalDateTime.class);
//            ReflectionTestUtils.setField(waitinglist2, BaseEntity.class, "createdAt", LocalDateTime.of(2021, 12, 6, 13, 0), LocalDateTime.class);
//
//            List<FindStudentInfoFromEnrollmentByLectureResponse> response = new ArrayList<>();
//            FindStudentInfoFromEnrollmentByLectureResponse response1 = FindStudentInfoFromEnrollmentByLectureResponse.builder().lectureId(lecture.getId()).waitingId(waitinglist.getId()).studentName(student.getName()).waitingNum(1L).build();
//            FindStudentInfoFromEnrollmentByLectureResponse response2 = FindStudentInfoFromEnrollmentByLectureResponse.builder().lectureId(lecture.getId()).waitingId(waitinglist2.getId()).studentName(student2.getName()).waitingNum(2L).build();
//            response.add(response1);
//            response.add(response2);
//
//            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
//            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
//            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
//            given(waitinglistRepository.findByLectureOrderByCreatedAtAsc(lecture)).willReturn(waitinglists);
//            given(waitinglists.stream()
//                    .map(waitinglist -> new FindStudentInfoFromEnrollmentByLectureResponse(waitinglist.getStudent(), waitinglist.getId(), waitinglist.getLecture().getId())).collect(Collectors.toList()))
//                    .willReturn(response);
//
////                    .willReturn(response);
////            willReturn(response).given(waitinglists).stream()
////                    .map(waitinglist -> new FindStudentInfoFromEnrollmentByLectureResponse(waitinglist.getStudent(), waitinglist.getId(), waitinglist.getLecture().getId())).collect(Collectors.toList());
////            willReturn(response).given(waitinglists).stream()
////                    .map(waitinglist -> new FindStudentInfoFromEnrollmentByLectureResponse(waitinglist.getStudent(), waitinglist.getId(), waitinglist.getLecture().getId())).collect(Collectors.toList());
//
////            willReturn(response).given(waitinglistRepository).findByLectureOrderByCreatedAtAsc(mockLecture)
////                    .stream().map(waitinglist -> new FindStudentInfoFromEnrollmentByLectureResponse(any(Student.class),anyLong(),anyLong())).collect(Collectors.toList());
//
////            willReturn(List.of(FindStudentInfoFromEnrollmentByLectureResponse.builder().lectureId(lecture.getId()).waitingId(waitinglist.getId()).studentName(student.getName()).waitingNum(1L).build(),
////                    FindStudentInfoFromEnrollmentByLectureResponse.builder().lectureId(lecture.getId()).waitingId(waitinglist2.getId()).studentName(student2.getName()).waitingNum(2L).build())).
////                    given(waitinglistRepository).findByLectureOrderByCreatedAtAsc(any(Lecture.class));
//
//            List<FindStudentInfoFromEnrollmentByLectureResponse> responses = waitinglistService.findWaitingStudentByLecture(academy.getId(), lecture.getId(), employee.getAccount());
//            assertThat(responses.size()).isEqualTo(2);
//        }

        @Test
        @DisplayName("실패(1) - 학원이 존재하지 않을 때")
        void ui_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.findWaitingStudentByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("실패(2) - 직원이 해당 학원 소속이 아닐 때")
        void ui_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.findWaitingStudentByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 사용자를 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("실패(3) - 강좌가 존재하지 않을 때")
        void ui_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.findWaitingStudentByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 강좌를 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(lectureRepository).should(times(1)).findById(anyLong());
        }
    }
}