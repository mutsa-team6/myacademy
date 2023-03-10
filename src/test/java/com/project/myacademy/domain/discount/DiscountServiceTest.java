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
    @DisplayName("??????")
    class DiscountRead{

        PageRequest pageable = PageRequest.of(0, 20, Sort.Direction.DESC,"createdAt");

        @Test
        @DisplayName("???????????? ?????? ?????? ??????")
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
        @DisplayName("???????????? ?????? ?????? ??????(1) = ????????? ???????????? ?????? ???")
        void getAllDiscounts_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAllDiscounts(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("???????????? ?????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void getAllDiscounts_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAllDiscounts(academy.getId(), employee.getAccount(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ?????? ??????")
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
        @DisplayName("??????????????? ????????? ???????????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
        void getAppliedDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void getAppliedDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ?????? ??????(3) - ????????? ????????? ?????? ????????? ???????????? ?????? ???")
        void getAppliedDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ?????? ??????(4) - ?????? ????????? ????????? ?????? ????????? ???????????? ?????? ???")
        void getAppliedDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(employee));
            given(enrollmentRepository.findById(anyLong())).willReturn(Optional.of(enrollment));
            given(discountRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.getAppliedDiscount(academy.getId(), enrollment.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ??????????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(discountRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("??????")
    class DiscountCheck{

        CheckDiscountRequest checkDiscountRequest = new CheckDiscountRequest("discountName", 1L);

        @Test
        @DisplayName("???????????? ?????? ??????")
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
            assertThat(checkDiscountResponse.getMessage()).isEqualTo("discountName??? ??????????????????.");

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
        @DisplayName("???????????? ?????? ??????(1) - ????????? ???????????? ?????? ???")
        void checkDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("???????????? ?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void checkDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("???????????? ?????? ??????(3) - ????????? ????????? ????????? ????????? ?????? ???")
        void checkDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("???????????? ?????? ??????(4) - ?????? ????????? ?????? ????????? ???????????? ?????? ???")
        void checkDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ??????????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("???????????? ?????? ??????(5) - ??????????????? ????????? ?????? ????????? ???????????? ?????? ???")
        void checkDiscount_fail5() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));
            given(enrollmentRepository.findById(checkDiscountRequest.getEnrollmentId())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.checkDiscount(academy.getId(), checkDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(checkDiscountRequest.getEnrollmentId());
        }

//        @Test
//        @DisplayName("???????????? ?????? ??????(6) - ??????????????? ????????? ?????? ????????? ????????? ???????????? ?????? ???")
//        void checkDiscount_fail6() {
//
//        }
//
//        @Test
//        @DisplayName("???????????? ?????? ??????(7) - ??????????????? ????????? ?????? ????????? ????????? ???????????? ?????? ???")
//        void checkDiscount_fail7() {
//
//        }

        @Test
        @DisplayName("???????????? ?????? ??????(6) - ??????????????? ????????? ?????? ????????? ?????? ????????? ????????? ???")
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
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ???????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(enrollmentRepository).should(times(1)).findById(anyLong());
            then(mockEnrollment).should(times(1)).getPaymentYN();
        }

    }

    @Nested
    @DisplayName("??????")
    class DiscountCreate{

        CreateDiscountRequest createDiscountRequest = new CreateDiscountRequest("discountName", 50);

        @Test
        @DisplayName("?????? ??????")
        void createDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());
            given(discountRepository.save(any(Discount.class))).willReturn(discount);

            CreateDiscountResponse createdDiscount = discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount());
            assertThat(createdDiscount.getDiscountId()).isEqualTo(1L);
            assertThat(createdDiscount.getMessage()).isEqualTo("???????????? ?????? ??????");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).save(any(Discount.class));
        }

        @Test
        @DisplayName("?????? ??????(1) - ????????? ???????????? ?????? ???")
        void createDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void createDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ??????(3) - ????????? ??????????????? ????????? ????????? ?????? ???")
        void createDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("?????? ??????(4) - ??????????????? ?????? ???????????? ?????? ??????")
        void createDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);
            given(discountRepository.findByDiscountNameAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(discount));

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.createDiscount(academy.getId(), createDiscountRequest, employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_DISCOUNT);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ???????????? ?????? ?????? ???????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(mockEmployee).should(times(1)).getEmployeeRole();
            then(discountRepository).should(times(1)).findByDiscountNameAndAcademy(anyString(), any(Academy.class));
        }

    }

    @Nested
    @DisplayName("??????")
    class DiscountDelete{

        @Test
        @DisplayName("?????? ??????")
        void deleteDiscount_success() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.of(discount));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_STAFF);

            DeleteDiscountResponse deletedDiscount = discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount());
            assertThat(deletedDiscount.getDiscountId()).isEqualTo(1L);
            assertThat(deletedDiscount.getMessage()).isEqualTo("???????????? ?????? ??????");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }

        @Test
        @DisplayName("?????? ??????(1) - ????????? ???????????? ?????? ???")
        void deleteDiscount_fail1() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(2) - ?????? ???????????? ????????? ?????? ?????? ????????? ?????? ???")
        void deleteDiscount_fail2() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));
            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("????????? ???????????? ?????? ???????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
        }

        @Test
        @DisplayName("?????? ??????(3) - ????????? ??????????????? ???????????? ?????? ???")
        void deleteDiscount_fail3() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DISCOUNT_NOT_FOUND);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ??????????????? ?????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("?????? ??????(4) - ????????? ??????????????? ????????? ????????? ?????? ???")
        void deleteDiscount_fail4() {

            given(academyRepository.findById(anyLong())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(anyString(), any(Academy.class))).willReturn(Optional.of(mockEmployee));
            given(discountRepository.findById(anyLong())).willReturn(Optional.of(discount));
            given(mockEmployee.getEmployeeRole()).willReturn(EmployeeRole.ROLE_USER);

            AppException appException = assertThrows(AppException.class,
                    () -> discountService.deleteDiscount(academy.getId(), discount.getId(), employee.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
            assertThat(appException.getErrorCode().getMessage()).isEqualTo("?????? ????????? ????????? ????????? ??? ????????????.");

            then(academyRepository).should(times(1)).findById(anyLong());
            then(employeeRepository).should(times(1)).findByAccountAndAcademy(anyString(), any(Academy.class));
            then(discountRepository).should(times(1)).findById(anyLong());
            then(mockEmployee).should(times(1)).getEmployeeRole();
        }
    }
}