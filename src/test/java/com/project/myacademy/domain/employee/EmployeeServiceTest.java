package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.domain.email.EmailService;
import com.project.myacademy.global.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Optional;

import static com.project.myacademy.domain.employee.EmployeeRole.ROLE_STAFF;
import static com.project.myacademy.domain.employee.EmployeeRole.ROLE_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private AcademyRepository academyRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private EmployeeService employeeService;
    private Academy academy;
    private Employee employeeADMIN, employeeSTAFF, employeeUSER;
    private Employee mockEmployee;
    private Pageable pageable;
    @Value("${jwt.token.secret}")
    String secretKey;

    @BeforeEach
    void setUp() {
        academy = Academy.builder().id(1L).name("??????").owner("??????").build();
        employeeADMIN = Employee.builder().id(1L).name("??????").account("admin").password("password").phoneNum("010-0000-0000").email("employeeADMIN@gmail.com").academy(academy).subject("????????????").employeeRole(EmployeeRole.ROLE_ADMIN).build();
        employeeSTAFF = Employee.builder().id(2L).name("??????").account("staff").password("password").phoneNum("010-0000-0001").email("employeeSTAFF@gmail.com").academy(academy).subject("??????").employeeRole(ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(3L).name("??????").account("user").password("password").phoneNum("010-0000-0002").email("employeeUSER@gmail.com").academy(academy).subject("??????").employeeRole(ROLE_USER).build();
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
        mockEmployee = mock(Employee.class);
    }

    @Nested
    @DisplayName("?????? ??????")
    class CreateEmployeeADMIN {

        @Test
        @DisplayName("?????? ?????? ??????")
        void create_employee_admin_success() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("??????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "1", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeADMIN);

            CreateEmployeeResponse response = employeeService.createEmployee(requestADMIN, academy.getId());

            assertThat(response.getName()).isEqualTo("??????");
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("?????? ?????? ??????1 - ?????? ??????")
        void create_employee_admin_fail1() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("??????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "1", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ACCOUNT);
        }

        @Test
        @DisplayName("?????? ?????? ??????2 - ????????? ??????")
        void create_employee_admin_fail2() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("??????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "1", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByEmail(anyString())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("?????? ?????? ??????3 - ????????? admin????????? ??????????????? ???????????? ????????????")
        void create_employee_admin_fail3() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("????????????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "1", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_OWNER);
        }

        @Test
        @DisplayName("?????? ?????? ??????4 - ???????????? ????????????")
        void create_employee_admin_fail4() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("??????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "0", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_EMPLOYEE_TYPE);
        }

        @Test
        @DisplayName("?????? ?????? ??????5 - ???????????? ??????")
        void create_employee_admin_fail5() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("??????", "????????????", "010-0000-0001", "??????@gmail.com", "admin", "password", "1", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class CreateEmployeeSTAFF {

        @Test
        @DisplayName("?????? ?????? ??????")
        void create_employee_staff_success() {

            CreateEmployeeRequest requestSTAFF = new CreateEmployeeRequest("??????", "????????????", "010-0000-0002", "??????@gmail.com", "staff", "password", "", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestSTAFF.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeSTAFF);

            CreateEmployeeResponse response = employeeService.createEmployee(requestSTAFF, academy.getId());

            assertThat(response.getName()).isEqualTo("??????");
            assertThat(response.getAccount()).isEqualTo("staff");
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class CreateEmployeeUSER {

        @Test
        @DisplayName("?????? ?????? ??????")
        void create_employee_user_success() {

            CreateEmployeeRequest requestUSER = new CreateEmployeeRequest("??????", "????????????", "010-0000-0003", "??????@gmail.com", "user", "password", "USER", "??????");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestUSER.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeUSER);

            CreateEmployeeResponse response = employeeService.createEmployee(requestUSER, academy.getId());

            assertThat(response.getName()).isEqualTo("??????");
            assertThat(response.getAccount()).isEqualTo("user");
        }

        @Test
        @DisplayName("?????? ?????? ??????")
        void create_employee_user_fail1() {

            CreateEmployeeRequest emptySubRequestUSER = new CreateEmployeeRequest("??????", "????????????", "010-0000-0003", "??????@gmail.com", "user", "password", "USER", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(emptySubRequestUSER, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("?????????")
    class LoginEmployee {

        LoginEmployeeRequest request = new LoginEmployeeRequest("admin", "password");

        @Test
        @DisplayName("????????? ??????")
        void login_employee_success() {

            MockedStatic<JwtTokenUtil> jwtTokenUtilMockedStatic = mockStatic(JwtTokenUtil.class);

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(), any())).willReturn(true);
            given(JwtTokenUtil.createToken(employeeADMIN.getAccount(), employeeADMIN.getEmail(), secretKey)).willReturn("AccessToken");
            given(JwtTokenUtil.createRefreshToken(secretKey)).willReturn("refreshToken");

            LoginEmployeeResponse response = employeeService.loginEmployee(request, academy.getId());

            assertThat(response.getJwt()).isEqualTo("AccessToken");
            assertThat(response.getEmployeeName()).isEqualTo("??????");

            jwtTokenUtilMockedStatic.close();
        }

        @Test
        @DisplayName("????????? ??????1 - ???????????? ?????? ????????? ??????")
        void login_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("????????? ??????2 - ???????????? ?????? ????????? ??????")
        void login_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("????????? ??????3 - ???????????? ??????")
        void login_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(), any())).willReturn(false);

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class FindAccountEmployee {
        FindAccountEmployeeRequest request = new FindAccountEmployeeRequest("??????", "employeeADMIN@gmail.com");

        @Test
        @DisplayName("?????? ?????? ??????")
        void find_account_employee_success() {
            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(employeeADMIN));

            FindAccountEmployeeResponse response = employeeService.findAccountEmployee(request);

            assertThat(response.getEmployeeId()).isEqualTo(1L);
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("?????? ?????? ?????? - ???????????? ?????? ?????? ??????")
        void find_account_employee_fail() {
            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findAccountEmployee(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("???????????? ??????(????????????????????? ????????? ???????????? ????????????)")
    class FindPasswordEmployee {
        FindPasswordEmployeeRequest request = new FindPasswordEmployeeRequest("admin", "employeeADMIN@gmail.com");

        @Test
        @DisplayName("???????????? ?????? ??????")
        void change_password_employee_success() throws MessagingException {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(mockEmployee));
            given(bCryptPasswordEncoder.encode(any())).willReturn(employeeADMIN.getPassword());

            willDoNothing().given(mockEmployee).updatePasswordOnly(any());
            given(employeeRepository.save(any())).willReturn(employeeADMIN);
//            willDoNothing().given(emailService).sendEmail(anyString(), anyString(), anyString());

            FindPasswordEmployeeResponse response = employeeService.findPasswordEmployee(request);

            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("???????????? ?????? ??????(1) - ???????????? ???????????? ??????")
        void change_password_employee_fail1() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findPasswordEmployee(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("?????? ???????????? ??????")
    class ChangePasswordEmployee {

        @Test
        @DisplayName("???????????? ?????? ??????")
        void change_password_success(){

            ChangePasswordEmployeeRequest request = new ChangePasswordEmployeeRequest("password", "changedPassword");
            String changedPassword = bCryptPasswordEncoder.encode(request.getNewPassword());

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(),any())).willReturn(true);
            given(bCryptPasswordEncoder.encode(any())).willReturn(changedPassword);

            ChangePasswordEmployeeResponse response = employeeService.changePasswordEmployee(request, academy.getId(), employeeADMIN.getAccount());

            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("???????????? ?????? ??????1 - ?????? ???????????? ??????")
        void change_password_fail1(){

            ChangePasswordEmployeeRequest request = new ChangePasswordEmployeeRequest("wrongPassword", "changedPassword");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(),any())).willReturn(false);

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.changePasswordEmployee(request, academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
        }

        @Test
        @DisplayName("???????????? ?????? ??????2 - ?????? ??????????????? ??????")
        void change_password_fail2(){

            ChangePasswordEmployeeRequest request = new ChangePasswordEmployeeRequest("password", "password");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(),any())).willReturn(true);

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.changePasswordEmployee(request, academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.SAME_PASSWORD);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class DeleteEmployee {

        @Test
        @DisplayName("?????? ?????? ??????")
        void delete_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            DeleteEmployeeResponse response = employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeSTAFF.getId());
        }

        @Test
        @DisplayName("?????? ?????? ??????1 - ???????????? ?????? ?????? ??????")
        void delete_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ??????2 - ????????? ????????? ????????? ???????????? ??????")
        void delete_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ??????3 - ??????????????? ????????? ???????????? ??????")
        void delete_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ??????4 - ??????????????? ????????? ?????? ???????????????")
        void delete_employee_fail4() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.BAD_DELETE_REQUEST);
        }

        @Test
        @DisplayName("?????? ?????? ??????5 - ??????????????? ????????? ADMIN?????????")
        void delete_employee_fail5() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.BAD_DELETE_REQUEST);
        }

        @Test
        @DisplayName("?????? ?????? ??????6 - ????????? ADMIN??? ???????????? ")
        void delete_employee_fail6() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeSTAFF.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("?????? ?????? (???????????? ?????? ????????????) ??????")
        void delete_myself_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            DeleteEmployeeResponse response = employeeService.selfDeleteEmployee(employeeSTAFF.getAccount(), academy.getId());

            assertThat(response.getEmployeeId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("?????? ?????? (???????????? ?????? ????????????) ?????? - ADMIN????????? ?????? ?????????")
        void delete_myself_fail() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.selfDeleteEmployee(employeeADMIN.getAccount(), academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_ALLOWED_CHANGE);
        }
    }

    @Nested
    @DisplayName("???????????? ????????? ??????????????? ??????")
    class ReadEmployee {

        @Test
        @DisplayName("??????????????? ?????? ??????")
        void read_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            ReadEmployeeResponse response = employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount());

            assertThat(response.getId()).isEqualTo(1l);
            assertThat(response.getName()).isEqualTo("??????");
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("??????????????? ?????? ??????1 - ???????????? ?????? ?????? ??????")
        void read_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("??????????????? ?????? ??????2 - ???????????? ?????? ?????? ??????")
        void read_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class findEmployee {

        @Test
        @DisplayName("Account??? Email??? ???????????? ??????")
        void find_by_account_email_success() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(employeeADMIN));

            Employee employee = employeeService.findByEmail(employeeADMIN.getEmail());

            assertThat(employee).isEqualTo(employeeADMIN);
        }

        @Test
        @DisplayName("Account??? Email??? ???????????? ?????? - ???????????? ?????? ?????? ??????")
        void find_by_account_email_fail() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findByEmail(employeeADMIN.getEmail()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????")
        void find_all_employee_success() {

            Page<Employee> employeeList = new PageImpl<>(List.of(employeeSTAFF,employeeUSER));

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findAllEmployee(any(), any())).willReturn(employeeList);

            Page<ReadAllEmployeeResponse> responses = employeeService.readAllEmployees(employeeADMIN.getAccount(), academy.getId(), pageable);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????1 - ???????????? ?????? ?????? ??????")
        void find_all_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeADMIN.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????2 - ???????????? ?????? ?????? ??????")
        void find_all_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeADMIN.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????3 - admin ????????? ??????")
        void find_all_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeSTAFF.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("??????????????? ???????????? ?????? ??????")
        void find_all_teacher_success() {

            Page<Employee> teacherList = new PageImpl<>(List.of(employeeUSER, employeeUSER));

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findAllTeacher(academy, pageable)).willReturn(teacherList);

            Page<ReadEmployeeResponse> responses = employeeService.findAllTeachers(employeeADMIN.getAccount(), academy.getId(), pageable);

            assertThat(responses.getTotalPages()).isEqualTo(1);
            assertThat(responses.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ??????")
        void find_teacher_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            ReadEmployeeResponse response = employeeService.findOneTeacher(employeeADMIN.getAccount(), academy.getId(), employeeUSER.getId());

            assertThat(response.getAccount()).isEqualTo(employeeUSER.getAccount());
            assertThat(response.getEmployeeRole()).isEqualTo(ROLE_USER);
        }

        @Test
        @DisplayName("?????? ?????? ?????? ?????? - ?????? ???????????? ????????? ?????????")
        void find_teacher_fail() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findOneTeacher(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_TEACHER);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class ChangeRoleEmployee {

        @Test
        @DisplayName("?????? ?????? ??????1 - USER -> STAFF")
        void change_role_employee_success1() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(employeeADMIN.getAccount(), academy.getId(), employeeUSER.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeUSER.getId());
            assertThat(response.getMessage()).isEqualTo(employeeUSER.getAccount() + " ????????? ????????? " + ROLE_STAFF + "??? ??????????????????");
        }

        @Test
        @DisplayName("?????? ?????? ??????2 - STAFF -> USER")
        void change_role_employee_success2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeSTAFF.getId());
            assertThat(response.getMessage()).isEqualTo(employeeSTAFF.getAccount() + " ????????? ????????? " + ROLE_USER + "??? ??????????????????");
        }

        @Test
        @DisplayName("?????? ?????? ??????1 - ADMIN??? ???????????? ??????")
        void change_role_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.changeRoleEmployee(employeeSTAFF.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        @Test
        @DisplayName("?????? ?????? ??????2 - ??????????????? ???????????? ??????")
        void change_role_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.changeRoleEmployee(employeeSTAFF.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.BAD_CHANGE_REQUEST);
        }
    }

    @Nested
    @DisplayName("?????? ??????")
    class UpdateEmployee {

        UpdateEmployeeRequest request = new UpdateEmployeeRequest("????????????", "????????????", "????????????");

        @Test
        @DisplayName("?????? ?????? ??????")
        void update_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            UpdateEmployeeResponse response = employeeService.updateEmployee(request, employeeADMIN.getAccount(), academy.getId());

            assertThat(response.getEmployeeId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("?????? ?????? ??????(1) - ???????????? ?????? ?????? ??????")
        void update_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.updateEmployee(request, employeeADMIN.getAccount(), academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("?????? ?????? ??????(2) - ???????????? ?????? ?????? ??????")
        void update_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.updateEmployee(request, employeeADMIN.getAccount(), academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("UI")
    class ui{

        @Test
        @DisplayName("?????? ??? ?????? ??? ?????? ??????")
        void countEmployees_byAcademy_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.countByAcademy(any(Academy.class))).willReturn(1L);

            Long count = employeeService.countEmployeesByAcademy(academy.getId());

            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("?????? ??? ?????? ??? ?????? ??????(1) - ???????????? ?????? ?????? ??????")
        void countEmployees_byAcademy_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.countEmployeesByAcademy(academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getMessage()).isEqualTo("?????? ????????? ?????? ??? ????????????.");
        }
    }
}
