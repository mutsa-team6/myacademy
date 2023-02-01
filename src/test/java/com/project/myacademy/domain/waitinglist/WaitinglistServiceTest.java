package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.ReadAllEnrollmentResponse;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
    private WaitinglistRepository waitinglistRepository;
    @InjectMocks
    private WaitinglistService waitinglistService;

    private Academy academy;
    private Employee employee;
    private Employee teacher;
    private Lecture lecture;
    private Student student;
    private Student student2;
    private Waitinglist waitinglist;
    private Waitinglist waitinglist2;
    private Employee mockEmployee;
    private Lecture mockLecture;


    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).maximumCapacity(10).currentEnrollmentNumber(0).build();
        student = Student.builder().id(1L).name("student").academyId(academy.getId()).build();
        student2 = Student.builder().id(2L).name("student2").academyId(academy.getId()).build();
        waitinglist = Waitinglist.builder().id(1L).student(student).lecture(lecture).build();
        waitinglist2 = Waitinglist.builder().id(2L).student(student2).lecture(lecture).build();
        mockEmployee = mock(Employee.class);
        mockLecture = mock(Lecture.class);
    }

    @Nested
    @DisplayName("조회")
    class WaitingListRead{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "createdAt");

        @Test
        @DisplayName("대기번호 리스트 조회 성공")
        public void readAllWaitingList_success() {

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
        public void readAllWaitingList_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.readAllWaitinglists(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("대기번호 리스트 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void readAllWaitingList_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.readAllWaitinglists(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 성공")
        public void countWaitingList_ByLecture_success() {

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
        public void countWaitingList_ByLecture_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void countWaitingList_ByLecture_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("강좌의 대기번호 수 조회 실패(3) - 대기번호 조회 대상인 강좌가 존재하지 않을 때")
        public void countWaitingList_ByLecture_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.countWaitingListByLecture(academy.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

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
        public void createWaitingList_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(waitinglistRepository.findByStudentAndLecture(any(Student.class), any(Lecture.class))).willReturn(Optional.empty());
            given(waitinglistRepository.saveAndFlush(any(Waitinglist.class))).willReturn(waitinglist);

            CreateWaitinglistResponse savedWaitingList = waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount());
            assertThat(savedWaitingList.getWaitinglistId()).isEqualTo(1L);
            assertThat(savedWaitingList.getMessage()).isEqualTo("대기번호 등록 완료");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(waitinglistRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
            then(waitinglistRepository).should(times(1)).saveAndFlush(any(Waitinglist.class));
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        public void createWaitingList_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        public void createWaitingList_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 계정명을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 대기번호에 등록될 학생이 존재하지 않을 때")
        public void createWaitingList_fail3() {

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
        public void createWaitingList_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.LECTURE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수업을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(5) - 직원이 대기번호를 등록할 권한이 아닐 때")
        public void createWaitingList_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> waitinglistService.createWaitinglist(academy.getId(), student.getId(), lecture.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(studentRepository).should(times(1)).findById(anyLong());
            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(6) - 대기번호가 중복 등록되어 있는 경우")
        public void createWaitingList_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(studentRepository.findById(anyLong())).willReturn(Optional.of(student));
            given(lectureRepository.findById(anyLong())).willReturn(Optional.of(lecture));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
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
            then(waitinglistRepository).should(times(1)).findByStudentAndLecture(any(Student.class), any(Lecture.class));
        }
    }
}