package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.EmailUtil;
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
    private EmailUtil emailUtil;
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
        academy = Academy.builder().id(1L).name("학원").owner("원장").build();
        employeeADMIN = Employee.builder().id(1L).name("원장").account("admin").password("password").phoneNum("010-0000-0000").email("employeeADMIN@gmail.com").academy(academy).subject("원장과목").employeeRole(EmployeeRole.ROLE_ADMIN).build();
        employeeSTAFF = Employee.builder().id(2L).name("직원").account("staff").password("password").phoneNum("010-0000-0001").email("employeeSTAFF@gmail.com").academy(academy).subject("직원").employeeRole(ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(3L).name("강사").account("user").password("password").phoneNum("010-0000-0002").email("employeeUSER@gmail.com").academy(academy).subject("수학").employeeRole(ROLE_USER).build();
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
        mockEmployee = mock(Employee.class);
    }

    @Nested
    @DisplayName("원장 등록")
    class CreateEmployeeADMIN {

        @Test
        @DisplayName("원장 등록 성공")
        void create_employee_admin_success() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "과목");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByEmail(anyString())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeADMIN);

            CreateEmployeeResponse response = employeeService.createEmployee(requestADMIN, academy.getId());

            assertThat(response.getName()).isEqualTo("원장");
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("원장 등록 실패1 - 계정 중복")
        void create_employee_admin_fail1() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "과목");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_ACCOUNT);
        }

        @Test
        @DisplayName("원장 등록 실패2 - 이메일 중복")
        void create_employee_admin_fail2() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "과목");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByEmail(anyString())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
        }

        @Test
        @DisplayName("원장 등록 실패3 - 계정이 admin이지만 대표자명과 일치하지 않는경우")
        void create_employee_admin_fail3() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장아님", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "코딩");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_OWNER);
        }

        @Test
        @DisplayName("원장 등록 실패4 - 직원유형 입력안함")
        void create_employee_admin_fail4() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "0", "코딩");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_EMPLOYEE_TYPE);
        }

        @Test
        @DisplayName("원장 등록 실패5 - 과목입력 안함")
        void create_employee_admin_fail5() {

            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("직원 등록")
    class CreateEmployeeSTAFF {

        @Test
        @DisplayName("직원 등록 성공")
        void create_employee_staff_success() {

            CreateEmployeeRequest requestSTAFF = new CreateEmployeeRequest("직원", "직원주소", "010-0000-0002", "직원@gmail.com", "staff", "password", "", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestSTAFF.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeSTAFF);

            CreateEmployeeResponse response = employeeService.createEmployee(requestSTAFF, academy.getId());

            assertThat(response.getName()).isEqualTo("직원");
            assertThat(response.getAccount()).isEqualTo("staff");
        }
    }

    @Nested
    @DisplayName("강사 등록")
    class CreateEmployeeUSER {

        @Test
        @DisplayName("강사 등록 성공")
        void create_employee_user_success() {

            CreateEmployeeRequest requestUSER = new CreateEmployeeRequest("강사", "강사주소", "010-0000-0003", "강사@gmail.com", "user", "password", "USER", "코딩");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestUSER.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeUSER);

            CreateEmployeeResponse response = employeeService.createEmployee(requestUSER, academy.getId());

            assertThat(response.getName()).isEqualTo("강사");
            assertThat(response.getAccount()).isEqualTo("user");
        }

        @Test
        @DisplayName("강사 등록 실패")
        void create_employee_user_fail1() {

            CreateEmployeeRequest emptySubRequestUSER = new CreateEmployeeRequest("강사", "강사주소", "010-0000-0003", "강사@gmail.com", "user", "password", "USER", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(emptySubRequestUSER, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPTY_SUBJECT_FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("로그인")
    class LoginEmployee {

        LoginEmployeeRequest request = new LoginEmployeeRequest("admin", "password");

        @Test
        @DisplayName("로그인 성공")
        void login_employee_success() {

            MockedStatic<JwtTokenUtil> jwtTokenUtilMockedStatic = mockStatic(JwtTokenUtil.class);

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(), any())).willReturn(true);
            given(JwtTokenUtil.createToken(employeeADMIN.getAccount(), employeeADMIN.getEmail(), secretKey)).willReturn("AccessToken");
            given(JwtTokenUtil.createRefreshToken(secretKey)).willReturn("refreshToken");

            LoginEmployeeResponse response = employeeService.loginEmployee(request, academy.getId());

            assertThat(response.getJwt()).isEqualTo("AccessToken");
            assertThat(response.getEmployeeName()).isEqualTo("원장");

            jwtTokenUtilMockedStatic.close();
        }

        @Test
        @DisplayName("로그인 실패1 - 일치하는 학원 정보가 없음")
        void login_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("로그인 실패2 - 일치하는 직원 정보가 없음")
        void login_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("로그인 실패3 - 비밀번호 틀림")
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
    @DisplayName("계정 찾기")
    class FindAccountEmployee {
        FindAccountEmployeeRequest request = new FindAccountEmployeeRequest("원장", "employeeADMIN@gmail.com");

        @Test
        @DisplayName("계정 찾기 성공")
        void find_account_employee_success() {
            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(employeeADMIN));

            FindAccountEmployeeResponse response = employeeService.findAccountEmployee(request);

            assertThat(response.getEmployeeId()).isEqualTo(1L);
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("계정 찾기 실패 - 일치하는 직원 정보 없음")
        void find_account_employee_fail() {
            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findAccountEmployee(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("비밀번호 찾기(임시비밀번호로 변경해 이메일로 전송해줌)")
    class FindPasswordEmployee {
        FindPasswordEmployeeRequest request = new FindPasswordEmployeeRequest("admin", "employeeADMIN@gmail.com");

        @Test
        @DisplayName("비밀번호 찾기 성공")
        void change_password_employee_success() throws MessagingException {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(mockEmployee));
            given(bCryptPasswordEncoder.encode(any())).willReturn(employeeADMIN.getPassword());

            willDoNothing().given(mockEmployee).updatePasswordOnly(any());
            given(employeeRepository.save(any())).willReturn(employeeADMIN);
            willDoNothing().given(emailUtil).sendEmail(anyString(), anyString(), anyString());

            FindPasswordEmployeeResponse response = employeeService.findPasswordEmployee(request);

            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("비밀번호 찾기 실패(1) - 이메일이 존재하지 않음")
        void change_password_employee_fail1() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findPasswordEmployee(request));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("직원 비밀번호 변경")
    class ChangePasswordEmployee {

        @Test
        @DisplayName("비밀번호 변경 성공")
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
        @DisplayName("비밀번호 변경 실패1 - 기존 패스워드 틀림")
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
        @DisplayName("비밀번호 변경 실패2 - 기존 패스워드와 같음")
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
    @DisplayName("직원 삭제")
    class DeleteEmployee {

        @Test
        @DisplayName("직원 삭제 성공")
        void delete_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            DeleteEmployeeResponse response = employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeSTAFF.getId());
        }

        @Test
        @DisplayName("직원 삭제 실패1 - 일치하는 학원 정보 없음")
        void delete_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("직원 삭제 실패2 - 삭제를 요청한 계정이 존재하지 않음")
        void delete_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("직원 삭제 실패3 - 삭제하려는 계정이 존재하지 않음")
        void delete_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("직원 삭제 실패4 - 삭제하려는 계정이 자기 자신인경우")
        void delete_employee_fail4() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.BAD_DELETE_REQUEST);
        }

        @Test
        @DisplayName("직원 삭제 실패5 - 삭제하려는 계정이 ADMIN인경우")
        void delete_employee_fail5() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeADMIN.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.BAD_DELETE_REQUEST);
        }

        @Test
        @DisplayName("직원 삭제 실패6 - 권한이 ADMIN이 아닌경우 ")
        void delete_employee_fail6() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.deleteEmployee(employeeSTAFF.getAccount(), academy.getId(), employeeSTAFF.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("회원 탈퇴 (로그인한 계정 회원탈퇴) 성공")
        void delete_myself_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            DeleteEmployeeResponse response = employeeService.selfDeleteEmployee(employeeSTAFF.getAccount(), academy.getId());

            assertThat(response.getEmployeeId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("회원 탈퇴 (로그인한 계정 회원탈퇴) 실패 - ADMIN계정은 삭제 불가함")
        void delete_myself_fail() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.selfDeleteEmployee(employeeADMIN.getAccount(), academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_ALLOWED_CHANGE);
        }
    }

    @Nested
    @DisplayName("로그인한 계정의 마이페이지 조회")
    class ReadEmployee {

        @Test
        @DisplayName("마이페이지 조회 성공")
        void read_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            ReadEmployeeResponse response = employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount());

            assertThat(response.getId()).isEqualTo(1l);
            assertThat(response.getName()).isEqualTo("원장");
            assertThat(response.getAccount()).isEqualTo("admin");
        }

        @Test
        @DisplayName("마이페이지 조회 실패1 - 일치하는 학원 정보 없음")
        void read_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("마이페이지 조회 실패2 - 일치하는 직원 정보 없음")
        void read_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readEmployee(academy.getId(), employeeADMIN.getAccount()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("직원 찾기")
    class findEmployee {

        @Test
        @DisplayName("Account와 Email로 직원찾기 성공")
        void find_by_account_email_success() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.of(employeeADMIN));

            Employee employee = employeeService.findByEmail(employeeADMIN.getEmail());

            assertThat(employee).isEqualTo(employeeADMIN);
        }

        @Test
        @DisplayName("Account와 Email로 직원찾기 실패 - 일치하는 계정 정보 없음")
        void find_by_account_email_fail() {

            given(employeeRepository.findByEmail(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.findByEmail(employeeADMIN.getEmail()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_FOUND);
        }

        @Test
        @DisplayName("모든 직원 찾기 성공")
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
        @DisplayName("모든 직원 찾기 실패1 - 일치하는 학원 정보 없음")
        void find_all_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeADMIN.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("모든 직원 찾기 실패2 - 일치하는 직원 정보 없음")
        void find_all_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeADMIN.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND);
        }

        @Test
        @DisplayName("모든 직원 찾기 실패3 - admin 권한이 아님")
        void find_all_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.readAllEmployees(employeeSTAFF.getAccount(), academy.getId(), pageable));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.INVALID_PERMISSION);
        }

        @Test
        @DisplayName("특정학원의 강사전체 찾기 성공")
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
        @DisplayName("특정 강사 찾기 성공")
        void find_teacher_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            ReadEmployeeResponse response = employeeService.findOneTeacher(employeeADMIN.getAccount(), academy.getId(), employeeUSER.getId());

            assertThat(response.getAccount()).isEqualTo(employeeUSER.getAccount());
            assertThat(response.getEmployeeRole()).isEqualTo(ROLE_USER);
        }

        @Test
        @DisplayName("특정 강사 찾기 실패 - 해당 임직원이 강사가 아닐때")
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
    @DisplayName("권한 변경")
    class ChangeRoleEmployee {

        @Test
        @DisplayName("권한 변경 성공1 - USER -> STAFF")
        void change_role_employee_success1() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeUSER));

            ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(employeeADMIN.getAccount(), academy.getId(), employeeUSER.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeUSER.getId());
            assertThat(response.getMessage()).isEqualTo(employeeUSER.getAccount() + " 계정의 권한을 " + ROLE_STAFF + "로 변경했습니다");
        }

        @Test
        @DisplayName("권한 변경 성공2 - STAFF -> USER")
        void change_role_employee_success2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));

            ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(employeeADMIN.getAccount(), academy.getId(), employeeSTAFF.getId());

            assertThat(response.getEmployeeId()).isEqualTo(employeeSTAFF.getId());
            assertThat(response.getMessage()).isEqualTo(employeeSTAFF.getAccount() + " 계정의 권한을 " + ROLE_USER + "로 변경했습니다");
        }

        @Test
        @DisplayName("권한 변경 실패1 - ADMIN을 변경하려 할때")
        void change_role_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeSTAFF));
            given(employeeRepository.findByIdAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.changeRoleEmployee(employeeSTAFF.getAccount(), academy.getId(), employeeADMIN.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.NOT_ALLOWED_CHANGE);
        }

        @Test
        @DisplayName("권한 변경 실패2 - 자기자신을 변경하려 할때")
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
    @DisplayName("직원 수정")
    class UpdateEmployee {

        UpdateEmployeeRequest request = new UpdateEmployeeRequest("바뀐주소", "바뀐번호", "바뀐과목");

        @Test
        @DisplayName("직원 수정 성공")
        void update_employee_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            UpdateEmployeeResponse response = employeeService.updateEmployee(request, employeeADMIN.getAccount(), academy.getId());

            assertThat(response.getEmployeeId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("직원 수정 실패(1) - 일치하는 학원 정보 없음")
        void update_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.updateEmployee(request, employeeADMIN.getAccount(), academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
        }

        @Test
        @DisplayName("직원 수정 실패(2) - 일치하는 직원 정보 없음")
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
        @DisplayName("학원 별 직원 수 조회 성공")
        void countEmployees_byAcademy_success() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.countByAcademy(any(Academy.class))).willReturn(1L);

            Long count = employeeService.countEmployeesByAcademy(academy.getId());

            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("학원 별 직원 수 조회 실패(1) - 일치하는 학원 정보 없음")
        void countEmployees_byAcademy_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.countEmployeesByAcademy(academy.getId()));

            assertThat(appException.getErrorCode()).isEqualTo(ErrorCode.ACADEMY_NOT_FOUND);
            assertThat(appException.getMessage()).isEqualTo("해당 학원을 찾을 수 없습니다.");
        }
    }
}
