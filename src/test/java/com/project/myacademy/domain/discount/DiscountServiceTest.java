package com.project.myacademy.domain.discount;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.discount.dto.*;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private DiscountRepository discountRepository;
    @InjectMocks
    private DiscountService discountService;

    private Academy academy;
    private Employee employee;
    private Employee teacher;
    private Lecture lecture;
    private Student student;
    private Enrollment enrollment;
    private Discount discount;
    private Discount discount2;
    private Employee mockEmployee;
    private Enrollment mockEnrollment;

    @BeforeEach
    void setup() {
        academy = Academy.builder().id(1L).name("academy").owner("owner").build();
        employee = Employee.builder().id(1L).name("staff").email("email").account("account").password("password").employeeRole(EmployeeRole.ROLE_STAFF).academy(academy).build();
        teacher = Employee.builder().id(2L).name("teacher").email("email1").account("account1").employeeRole(EmployeeRole.ROLE_USER).academy(academy).build();
        lecture = Lecture.builder().id(1L).name("lecture").price(10000).employee(teacher).build();
        student = Student.builder().id(1L).name("student").academyId(academy.getId()).build();
        discount = Discount.builder().id(1L).discountName("discountName").discountRate(50).academy(academy).build();
        discount2 = Discount.builder().id(2L).discountName("discountName2").discountRate(40).academy(academy).build();
        enrollment = Enrollment.builder().id(1L).student(student).lecture(lecture).discountId(discount.getId()).build();
        mockEmployee = mock(Employee.class);
        mockEnrollment = mock(Enrollment.class);
    }

    @Nested
    @DisplayName("조회")
    class DiscountRead{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("할인정책 전체 조회 성공")
        void getAllDiscounts_success() {

            PageImpl<Discount> discountList = new PageImpl<>(List.of(discount, discount2));

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(discountRepository.findAllByAcademyAndDeletedAtIsNull(academy,pageable)).willReturn(discountList);

            Page<GetDiscountResponse> allDiscounts = discountService.getAllDiscounts(academy.getId(), employee.getAccount(), pageable);

            assertThat(allDiscounts.getTotalPages()).isEqualTo(1);
            assertThat(allDiscounts.getTotalElements()).isEqualTo(2);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findAllByAcademyAndDeletedAtIsNull(academy,pageable);
        }

        @Test
        @DisplayName("할인정책 전체 조회 실패(1) = 학원이 존재하지 않을 때")
        void getAllDiscounts_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAllDiscounts(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("할인정책 전체 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        void getAllDiscounts_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAllDiscounts(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("수강이력에 적용된 할인정책 조회 성공")
        void getAppliedDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(discountRepository.findById(anyLong())).willReturn(Optional.of(discount));

            GetAppliedDiscountResponse appliedDiscount = discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount());
            assertThat(appliedDiscount.getDiscountName()).isEqualTo("discountName");
            assertThat(appliedDiscount.getDiscountRate()).isEqualTo(50);

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(discountRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강이력에 적용된 할인정책 조회 실패(1) - 학원이 존재하지 않을 때")
        void getAppliedDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강이력에 적용된 할인정책 조회 실패(2) - 조회 진행하는 직원이 해당 학원 소속이 아닐 때")
        void getAppliedDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("수강이력에 적용된 할인정책 조회 실패(3) - 할인이 적용된 수강 이력이 존재하지 않을 때")
        void getAppliedDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강 이력을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("수강이력에 적용된 할인정책 조회 실패(4) - 수강 이력에 적용된 할인 정책이 존재하지 않을 때")
        void getAppliedDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(discountRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 할인정책을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(discountRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("적용")
    class DiscountCheck{

        CheckDiscountRequest checkDiscountRequest = new CheckDiscountRequest("discountName", 1L);

        @Test
        @DisplayName("할인정책 적용 성공")
        void checkDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(mockEnrollment));

//            given(studentRepository.findById(enrollment.getStudent().getId())).willReturn(Optional.of(student));
//            given(lectureRepository.findById(enrollment.getLecture().getId())).willReturn(Optional.of(lecture));
//            given(enrollmentRepository.findById(enrollment.getId())).willReturn(Optional.of(mockEnrollment));

            given(mockEnrollment.getPaymentYN()).willReturn(false);

            CheckDiscountResponse checkDiscountResponse = discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount());
            assertThat(checkDiscountResponse.getMessage()).isEqualTo("discountName을 적용했습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
//            then(studentRepository).should(times(1)).findById(anyLong());
//            then(lectureRepository).should(times(1)).findById(anyLong());
            then(mockEnrollment).should(times(1)).getPaymentYN();
        }

        @Test
        @DisplayName("할인정책 적용 실패(1) - 학원이 존재하지 않을 때")
        void checkDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("할인정책 적용 실패(2) - 적용 진행하는 직원이 해당 학원 소속이 아닐 때")
        void checkDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("할인정책 적용 실패(3) - 직원이 적용을 진행할 권한이 아닐 때")
        void checkDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("할인정책 적용 실패(4) - 적용 요청할 할인 정책이 존재하지 않을 때")
        void checkDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 할인정책을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("할인정책 적용 실패(5) - 할인정책이 적용될 수강 내역이 존재하지 않을 때")
        void checkDiscount_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));
            given(enrollmentRepository.findById(checkDiscountRequest.getEnrollmentId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 수강 이력을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(checkDiscountRequest.getEnrollmentId());
        }

//        @Test
//        @DisplayName("할인정책 적용 실패(6) - 할인정책에 적용할 수강 내역에 학생이 존재하지 않을 때")
//        void checkDiscount_fail6() {
//
//        }
//
//        @Test
//        @DisplayName("할인정책 적용 실패(7) - 할인정책에 적용할 수강 내역에 강좌가 존재하지 않을 때")
//        void checkDiscount_fail7() {
//
//        }

        @Test
        @DisplayName("할인정책 적용 실패(6) - 할인정책을 적용할 수강 내역이 이미 결제된 상태일 때")
        void checkDiscount_fail6() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(mockEnrollment));
            given(mockEnrollment.getPaymentYN()).willReturn(true);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_PAYMENT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 결제된 수업입니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEnrollment).should(times(1)).getPaymentYN();
        }

    }

    @Nested
    @DisplayName("등록")
    class DiscountCreate{

        CreateDiscountRequest createDiscountRequest = new CreateDiscountRequest("discountName", 50);

        @Test
        @DisplayName("등록 성공")
        void createDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());
            given(discountRepository.save(any(Discount.class))).willReturn(discount);

            CreateDiscountResponse createdDiscount = discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount());
            assertThat(createdDiscount.getDiscountId()).isEqualTo(1L);
            assertThat(createdDiscount.getMessage()).isEqualTo("할인정책 등록 성공");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).save(any(Discount.class));
        }

        @Test
        @DisplayName("등록 실패(1) - 학원이 존재하지 않을 때")
        void createDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("등록 실패(2) - 등록 진행하는 직원이 해당 학원 소속이 아닐 때")
        void createDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("등록 실패(3) - 직원이 할인정책을 등록할 권한이 아닐 때")
        void createDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("등록 실패(4) - 할인정책이 중복 등록되어 있는 경우")
        void createDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_DISCOUNT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("이미 할인 정책에 등록되어 있습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
        }

    }

    @Nested
    @DisplayName("삭제")
    class DiscountDelete{

        @Test
        @DisplayName("삭제 성공")
        void deleteDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.of(discount));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            DeleteDiscountResponse deletedDiscount = discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount());
            assertThat(deletedDiscount.getDiscountId()).isEqualTo(1L);
            assertThat(deletedDiscount.getMessage()).isEqualTo("할인정책 삭제 성공");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("삭제 실패(1) - 학원이 존재하지 않을 때")
        void deleteDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(2) - 삭제 진행하는 직원이 해당 학원 소속이 아닐 때")
        void deleteDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("요청한 직원을 해당 학원에서 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("삭제 실패(3) - 삭제할 할인정책이 존재하지 않을 때")
        void deleteDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("해당 할인정책을 찾을 수 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("삭제 실패(4) - 직원이 할인정책을 삭제할 권한이 아닐 때")
        void deleteDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.of(discount));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("사용자가 권한이 없습니다.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }
}