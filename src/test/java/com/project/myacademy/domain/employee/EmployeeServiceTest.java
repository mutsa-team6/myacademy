package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.dto.CreateEmployeeRequest;
import com.project.myacademy.domain.employee.dto.CreateEmployeeResponse;
import com.project.myacademy.domain.employee.dto.LoginEmployeeRequest;
import com.project.myacademy.domain.employee.dto.LoginEmployeeResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.EmailUtil;
import com.project.myacademy.global.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;


class EmployeeServiceTest {
    private EmployeeService employeeService;
    BCryptPasswordEncoder bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
    EmailUtil emailUtil;

    EmployeeRepository employeeRepository = Mockito.mock(EmployeeRepository.class);

    AcademyRepository academyRepository = Mockito.mock(AcademyRepository.class);

    @InjectMocks
    private Academy academy;
    private Employee employeeADMIN, employeeSTAFF, employeeUSER;
    private Pageable pageable;
    @Value("${jwt.token.secret}")
    String secretKey;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRepository, academyRepository, bCryptPasswordEncoder, emailUtil);
        academy = Academy.builder().id(1L).name("학원").owner("원장").build();
        employeeADMIN = Employee.builder().id(1L).name("원장").account("admin").password("password").phoneNum("010-0000-0000").email("employeeADMIN@gmail.com").academy(academy).employeeRole(EmployeeRole.ROLE_ADMIN).build();
        employeeSTAFF = Employee.builder().id(2L).name("직원").account("staff").password("password").phoneNum("010-0000-0001").email("employeeSTAFF@gmail.com").academy(academy).employeeRole(EmployeeRole.ROLE_STAFF).build();
        employeeUSER = Employee.builder().id(3L).name("강사").account("user").password("password").phoneNum("010-0000-0002").email("employeeUSER@gmail.com").academy(academy).employeeRole(EmployeeRole.ROLE_USER).build();
        pageable = PageRequest.of(0, 20, Sort.Direction.DESC, "id");
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
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeADMIN);

            CreateEmployeeResponse response = employeeService.createEmployee(requestADMIN, academy.getId());

            assertThat(response.getName().equals("원장"));
            assertThat(response.getAccount().equals("admin"));
        }

        @Test
        @DisplayName("원장 등록 실패5 - 계정 중복")
        void create_employee_admin_fail5() {
            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "과목");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.DUPLICATED_ACCOUNT));
        }

        @Test
        @DisplayName("원장 등록 실패4 - 이메일 중복")
        void create_employee_admin_fail4() {
            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "과목");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.of(employeeADMIN));

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.DUPLICATED_EMAIL));
        }

        @Test
        @DisplayName("원장 등록 실패3 - 계정이 admin이지만 대표자명과 일치하지 않는경우")
        void create_employee_admin_fail3() {
            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장아님", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "코딩");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.NOT_MATCH_OWNER));
        }

        @Test
        @DisplayName("원장 등록 실패2 - 직원유형 입력안함")
        void create_employee_admin_fail2() {
            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "0", "코딩");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPTY_SUBJECT_FORBIDDEN));
        }

        @Test
        @DisplayName("원장 등록 실패1 - 과목입력 안함")
        void create_employee_admin_fail1() {
            CreateEmployeeRequest requestADMIN = new CreateEmployeeRequest("원장", "원장주소", "010-0000-0001", "원장@gmail.com", "admin", "password", "1", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestADMIN.getPassword());

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(requestADMIN, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPTY_EMPLOYEE_TYPE));
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
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestSTAFF.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeSTAFF);

            CreateEmployeeResponse response = employeeService.createEmployee(requestSTAFF, academy.getId());

            assertThat(response.getName().equals("강사"));
            assertThat(response.getAccount().equals("staff"));
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
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(requestUSER.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeUSER);

            CreateEmployeeResponse response = employeeService.createEmployee(requestUSER, academy.getId());

            assertThat(response.getName().equals("강사"));
            assertThat(response.getAccount().equals("user"));
        }

        @Test
        @DisplayName("강사 등록 실패")
        void create_employee_user_fail1() {
            CreateEmployeeRequest emptySubRequestUSER = new CreateEmployeeRequest("강사", "강사주소", "010-0000-0003", "강사@gmail.com", "user", "password", "USER", "");

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());
            given(employeeRepository.findByNameAndEmail(any(), any())).willReturn(Optional.empty());
            given(bCryptPasswordEncoder.encode(any())).willReturn(emptySubRequestUSER.getPassword());
            given(employeeRepository.save(any())).willReturn(employeeUSER);

            AppException appException = assertThrows(AppException.class,
                    () -> employeeService.createEmployee(emptySubRequestUSER, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPTY_SUBJECT_FORBIDDEN));
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
            given(JwtTokenUtil.createToken(employeeADMIN.getAccount(), employeeADMIN.getEmail(), secretKey, 1000 * 60 * 60)).willReturn("token");

            LoginEmployeeResponse response = employeeService.loginEmployee(request, academy.getId());

            assertThat(response.getJwt().equals("token"));
            assertThat(response.getEmployeeName().equals("원장"));

            jwtTokenUtilMockedStatic.close();
        }

        @Test
        @DisplayName("로그인 실패1 - 일치하는 학원 정보가 없음")
        void login_employee_fail1() {

            given(academyRepository.findById(any())).willReturn(Optional.empty());

            AppException appException =  assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.ACADEMY_NOT_FOUND));
        }

        @Test
        @DisplayName("로그인 실패2 - 일치하는 직원 정보가 없음")
        void login_employee_fail2() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.empty());

            AppException appException =  assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.EMPLOYEE_NOT_FOUND));
        }

        @Test
        @DisplayName("로그인 실패3 - 비밀번호 틀림")
        void login_employee_fail3() {

            given(academyRepository.findById(any())).willReturn(Optional.of(academy));
            given(employeeRepository.findByAccountAndAcademy(any(), any())).willReturn(Optional.of(employeeADMIN));
            given(bCryptPasswordEncoder.matches(any(), any())).willReturn(false);

            AppException appException =  assertThrows(AppException.class,
                    () -> employeeService.loginEmployee(request, academy.getId()));

            assertThat(appException.getErrorCode().equals(ErrorCode.INVALID_PASSWORD));
        }
    }
}
