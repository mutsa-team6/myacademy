package com.project.myacademy.domain.student;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.parent.ParentRepository;
import com.project.myacademy.domain.student.dto.*;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ParentRepository parentRepository;
    @InjectMocks
    private StudentService studentService;
    private Academy academy;
    private Employee employeeSTAFF, employeeUSER;
    private Parent parent;
    private Student student1, student2, student3;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        academy = Academy.builder().id(1L).name("학원").build();
        employeeSTAFF = Employee.builder().id(1L).name("직원").account("employeeSTAFF@gmail.com").employeeRole(EmployeeRole.ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(2L).name("강사").account("employeeUSER@gmail.com").employeeRole(EmployeeRole.ROLE_USER).build();
        parent = Parent.builder().id(1L).name("부모").phoneNum("010-0000-0000").academyId(1L).build();
        student1 = Student.builder().id(1L).name("학생").phoneNum("010-1111-1111").email("student1@gmail.com").academyId(1L).parent(parent).build();
        student2 = Student.builder().id(2L).name("학생2").phoneNum("010-1111-1112").email("student2@gmail.com").academyId(1L).parent(parent).build();
        student3 = Student.builder().id(3L).name("학생").phoneNum("010-1111-1113").email("student3@gmail.com").academyId(1L).parent(parent).build(); // 동명이인
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
    }

    @Nested
    @DisplayName("학생 등록")
    class CreateStudent {
        CreateStudentRequest request = CreateStudentRequest.builder().name("학생").phoneNum("010-1111-1111").parentPhoneNum("010-0000-0000").email("student1@gmail.com").build();

        @Test
        @DisplayName("학생 등록 성공")
        void create_student_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(parentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(parent));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.empty());
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.empty());

            Student savedStudent = Student.toStudent(request, parent, academy.getId());
            given(studentRepository.save(any())).willReturn(savedStudent);

            CreateStudentResponse response = studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getName()).isEqualTo("학생");

        }

        @Test
        @DisplayName("학생 등록 실패1 - 일치하는 학원 정보 없음")
        void create_student_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 등록 실패2 - 일치하는 직원 정보 없음")
        void create_student_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 등록 실패3 - 일치하는 부모 정보 없음")
        void create_student_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(parentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.PARENT_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 등록 실패4 - 학생 핸드폰번호 중복")
        void create_student_fail4() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(parentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(parent));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(student1));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_STUDENT);
        }

        @Test
        @DisplayName("학생 등록 실패5 - 학생 이메일 중복")
        void create_student_fail5() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(parentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(parent));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.empty());
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.of(student1));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("학생 등록 실패6 - 직원 권한이 USER 일 때")
        void create_student_fail6() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.createStudent(academy.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }
    }

    @Nested
    @DisplayName("학생 조회")
    class readStudent {

        @Test
        @DisplayName("학생 조회 성공")
        void read_student_success() {

            given(academyRepository.findById((any()))).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));

            ReadStudentResponse response = studentService.readStudent(academy.getId(), student1.getId(), employeeUSER.getAccount());

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("학생");
            assertThat(response.getPhoneNum()).isEqualTo("010-1111-1111");
            assertThat(response.getParentPhoneNum()).isEqualTo("010-0000-0000");
        }

        @Test
        @DisplayName("학생 조회 실패1 - 일치하는 학원 정보 없음")
        void read_student_fail1() {

            given(academyRepository.findById((any()))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.readStudent(academy.getId(), student1.getId(), employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 조회 실패2 - 일치하는 직원 정보 없음")
        void read_student_fail2() {

            given(academyRepository.findById((any()))).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.readStudent(academy.getId(), student1.getId(), employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 조회 실패3 - 일치하는 학생 정보 없음")
        void read_student_fail3() {

            given(academyRepository.findById((any()))).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.readStudent(academy.getId(), student1.getId(), employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("학생 전체 조회")
    class readALLStudent {

        @Test
        @DisplayName("학생 전체 조회 성공")
        void read_students_success() {

            given(academyRepository.findById((any()))).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            Page<Student> studentList = new PageImpl<>(List.of(student1, student2));
            given(studentRepository.findAllByAcademyId(pageable, academy.getId())).willReturn(studentList);

            Page<ReadAllStudentResponse> responses = studentService.readAllStudent(academy.getId(), pageable, employeeUSER.getAccount());

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("학생 전체 조회 실패1 - 일치하는 학원 정보 없음")
        void read_students_fail1() {

            given(academyRepository.findById((any()))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.readAllStudent(academy.getId(), pageable, employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 전체 조회 실패2 - 일치하는 직원 정보 없음")
        void read_students_fail2() {

            given(academyRepository.findById((any()))).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.readAllStudent(academy.getId(), pageable, employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 이름으로 전체 조회")
        void read_by_name_students_success() {

            Page<Student> studentList = new PageImpl<>(List.of(student1, student3));

            given(studentRepository.findByAcademyIdAndName(any(), any(), any())).willReturn(studentList);

            Page<ReadAllStudentResponse> studentListResponse = studentService.findStudentForStudentList(academy.getId(), student1.getName(), pageable);

            assertThat(studentListResponse.getTotalElements()).isEqualTo(2);
            assertThat(studentListResponse.getTotalPages()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("학생 수정")
    class updateStudent {
        UpdateStudentRequest request = new UpdateStudentRequest("바뀐이름", "바뀐학교", "010-9999-9999", "changeEmail@gmail.com", "980525");

        @Test
        @DisplayName("학생 수정 성공")
        void update_student_success1() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(student1));

            UpdateStudentResponse response = studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("바뀐이름");
            assertThat(response.getEmail()).isEqualTo("changeEmail@gmail.com");
            assertThat(response.getPhoneNum()).isEqualTo("010-9999-9999");
            assertThat(response.getBirth()).isEqualTo("980525");
        }

        @Test
        @DisplayName("학생 수정 성공(2) - 변경될 이메일이 없는 경우")
        void update_student_success2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.empty());
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(student1));

            UpdateStudentResponse response = studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("바뀐이름");
            assertThat(response.getEmail()).isEqualTo("changeEmail@gmail.com");
            assertThat(response.getPhoneNum()).isEqualTo("010-9999-9999");
            assertThat(response.getBirth()).isEqualTo("980525");
        }

        @Test
        @DisplayName("학생 수정 성공(3) - 변경될 전화번호가 없는 경우")
        void update_student_success3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.empty());

            UpdateStudentResponse response = studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount());

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("바뀐이름");
            assertThat(response.getEmail()).isEqualTo("changeEmail@gmail.com");
            assertThat(response.getPhoneNum()).isEqualTo("010-9999-9999");
            assertThat(response.getBirth()).isEqualTo("980525");
        }

        @Test
        @DisplayName("학생 수정 실패1 - 일치하는 학원 정보 없음")
        void update_student_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 수정 실패2 - 일치하는 직원 정보 없음")
        void update_student_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 수정 실패3 - 일치하는 학생 정보 없음")
        void update_student_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), request, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 수정 실패4 - 이메일 중복")
        void update_student_fail4() {

            UpdateStudentRequest sameEmailRequest = new UpdateStudentRequest("바뀐이름", "바뀐학교", "010-1111-1111", "student2@gmail.com", "980525");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.of(student2));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), sameEmailRequest, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("학생 수정 실패5 - 핸드폰 번호 중복")
        void update_student_fail5() {

            UpdateStudentRequest samePhoneNumRequest = new UpdateStudentRequest("바뀐이름", "바뀐학교", "010-1111-1112", "changeEmail@gmail.com", "980525");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByEmailAndAcademyId(any(), any())).willReturn(Optional.of(student1));
            given(studentRepository.findByPhoneNumAndAcademyId(any(), any())).willReturn(Optional.of(student2));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), samePhoneNumRequest, employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_PHONENUM);
        }

        @Test
        @DisplayName("학생 수정 실패6 - 수정할 권한 없음")
        void update_student_fail6() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.updateStudent(academy.getId(), student1.getId(), request, employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }
    }

    @Nested
    @DisplayName("학생 삭제")
    class deleteStudent {

        @Test
        @DisplayName("학생 삭제 성공")
        void delete_student_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.of(student1));

            DeleteStudentResponse response = studentService.deleteStudent(academy.getId(), student1.getId(), employeeSTAFF.getAccount());

            assertThat(response.getId()).isEqualTo(student1.getId());
            assertThat(response.getName()).isEqualTo(student1.getName());
        }

        @Test
        @DisplayName("학생 삭제 실패1 - 일치하는 학원 정보 없음")
        void delete_student_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.deleteStudent(academy.getId(), student1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 삭제 실패2 - 일치하는 직원 정보 없음")
        void delete_student_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.deleteStudent(academy.getId(), student1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 삭제 실패3 - 일치하는 학생 정보 없음")
        void delete_student_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(studentRepository.findByAcademyIdAndId(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.deleteStudent(academy.getId(), student1.getId(), employeeSTAFF.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.STUDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("학생 삭제 실패4 - 삭제할 권한 없음")
        void delete_student_fail4() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.deleteStudent(academy.getId(), student1.getId(), employeeUSER.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }
    }

    @Nested
    @DisplayName("UI")
    class ui {

        @Test
        @DisplayName("학원 별 학생 수 조회 성공")
        void countStudent_byAcademy_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(studentRepository.countStudentByAcademyId(any())).willReturn(1L);

            Long count = studentService.countStudentByAcademy(academy.getId());

            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("학원 별 학생 수 조회 실패(1) - 일치하는 학원 정보 없음")
        void countStudent_byAcademy_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> studentService.countStudentByAcademy(academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");
        }
    }
}