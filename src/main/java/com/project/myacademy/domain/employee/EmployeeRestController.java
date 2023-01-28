package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/academies/")
@RequiredArgsConstructor
@Slf4j
public class EmployeeRestController {

    private final EmployeeService employeeService;
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 회원가입", description = "직원이 회원 가입을 합니다.")
    @PostMapping("/{academyId}/employees/signup")
    public ResponseEntity create(@PathVariable Long academyId, @RequestBody CreateEmployeeRequest request) {

        log.info("⭐ 회원가입 요청한 id [{}] 요청한 사용자 계정 [{}]", academyId, request.getAccount());

        CreateEmployeeResponse response = employeeService.createEmployee(request, academyId);


        return ResponseEntity.ok(Response.success(response));
    }

    @Tag(name = "02-2. 직원", description = "직원 로그인,계정 및 비밀번호 찾기")
    @Operation(summary = "직원 로그인", description =
            "회원가입된 계정과 비밀번호로 로그인합니다. \n\n 로그인시 쿠키에 토큰이 담김니다.")
    @PostMapping("/{academyId}/employees/login")
    public ResponseEntity login(@PathVariable Long academyId, @RequestBody LoginEmployeeRequest request, HttpServletRequest httpRequest, HttpServletResponse httpServletResponse) {

        log.info("✨ 로그인 요청한 학원 id [{}] 요청한 사용자 계정 [{}]", academyId, request.getAccount());

        LoginEmployeeResponse response = employeeService.loginEmployee(request, academyId);

        if (response.getJwt() != null) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("name", response.getEmployeeName());
        }
        String token = response.getJwt();
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.setCookieSecure(true);
        cookieGenerator.addCookie(httpServletResponse, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1시간
        log.info("🍪 쿠키에 저장한 토큰 {}", token);

        return ResponseEntity.ok(Response.success(response));
    }

    // 로그아웃
    @Tag(name = "0. 로그아웃", description = "스웨거용 API")
    @Operation(summary = "직원 로그아웃", description = "스웨거용 ENDPOINT. \n\n 로그아웃시 쿠키가 삭제됩니다.")
    @PostMapping("/employees/logout")
    public ResponseEntity logout(Authentication authentication, HttpServletResponse httpServletResponse) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("🔑 로그아웃을 요청한 계정 [{}]", requestAccount);
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.addCookie(httpServletResponse, "deleted");
        cookieGenerator.setCookieMaxAge(0);


        return ResponseEntity.ok(Response.success("로그아웃 성공"));
    }

    // 본인 정보 수정
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 수정", description = "ADMIN 회원 및 본인 만 수정이 가능합니다.")
    @PutMapping("/{academyId}")
    public ResponseEntity update(Authentication authentication, @PathVariable Long academyId, @RequestBody UpdateEmployeeRequest request) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info(" 🛠 본인 정보 수정을 요청한 사용자 계정 [{}] || 학원 아이디 [{}] ", requestAccount, academyId);

        UpdateEmployeeResponse response = employeeService.updateEmployee(request, requestAccount, academyId);

        return ResponseEntity.ok(Response.success(response));
    }

    // 본인 탈퇴 기능
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 본인 삭제", description ="ADMIN 회원 및 본인 만 삭제가 가능합니다.\n\n soft-delete 됩니다.")
    @DeleteMapping("/{academyId}")
    public ResponseEntity selfDelete(Authentication authentication, @PathVariable Long academyId) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info(" ❌ 본인 탈퇴를 요청한 사용자 계정 [{}] || 학원 아이디 [{}] ", requestAccount, academyId);

        DeleteEmployeeResponse response = employeeService.selfDeleteEmployee(requestAccount, academyId);

        return ResponseEntity.ok(Response.success(response));
    }

    /**
     * 계정명 찾기
     *
     * @param request
     * @return
     */
    @Tag(name = "02-2. 직원", description = "직원 로그인,계정 및 비밀번호 찾기")
    @Operation(summary = "직원 계정찾기", description = "직원 계정을 찾습니다.")
    @PostMapping("employee/findAccount")
    public ResponseEntity findAccount(@RequestBody FindAccountEmployeeRequest request) {

        FindAccountEmployeeResponse response = employeeService.findAccountEmployee(request);

        return ResponseEntity.ok(Response.success(response));
    }

    /**
     * 비밀번호 찾기
     *
     * @param request
     * @return
     */
    @Tag(name = "02-2. 직원", description = "직원 로그인,계정 및 비밀번호 찾기")
    @Operation(summary = "직원 비밀번호 찾기", description = "이메일로 임시 비밀번호가 발송됩니다.")
    @PutMapping("/employee/findPassword")
    public ResponseEntity changePassword(@RequestBody ChangePasswordEmployeeRequest request) {
        ChangePasswordEmployeeResponse response = employeeService.changePasswordEmployee(request);
        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원 계정을 삭제할 수 있다.
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다.")
    @DeleteMapping("/{academyId}/employees/{employeeId}")
    public ResponseEntity delete(Authentication authentication, @PathVariable Long academyId, @PathVariable Long employeeId) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info(" ❌ 삭제를 요청한 사용자 계정 [{}] || 학원 아이디 [{}] ", requestAccount, academyId);

        DeleteEmployeeResponse response = employeeService.deleteEmployee(requestAccount, academyId, employeeId);

        return ResponseEntity.ok(Response.success(response));
    }

    // 직원 마이페이지 조회
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 마이페이지 조회", description = "마이페이지를 조회합니다.")
    @GetMapping("/{academyId}/my")
    public ResponseEntity read(HttpServletRequest request, Authentication authentication, @PathVariable Long academyId) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info(" 🔎 마이페이지 조회를 요청한 사용자 계정 [{}] || 학원 아이디 [{}] ", requestAccount, academyId);

        ReadEmployeeResponse response = employeeService.readEmployee(academyId, requestAccount);
        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자(ADMIN) 회원만 접근할 수 있는, 전체 회원 보기
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 조회", description = "ADMIN 회원만 조회가 가능합니다.")
    @GetMapping("/{academyId}/employees")
    public ResponseEntity readAll(@PathVariable Long academyId, Authentication authentication, Pageable pageable) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("🔎 조회를 요청한 사용자 계정 [{}] || 접근하려는 학원 id [{}] ", requestAccount, academyId);

        Page<ReadEmployeeResponse> response = employeeService.readAllEmployees(requestAccount, academyId, pageable);

        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원의 등급을 USER -> STAFF 혹은 STAFF -> USER 로 변경할 수 있다.
    @Tag(name = "02-1. 직원", description = "직원 회원 가입 및 정보 수정,조회")
    @Operation(summary = "직원 권한 변경", description = "ADMIN,STAFF 회원만 권한변경이 가능합니다. \n\n User ↔ STAFF")
    @PutMapping("/{academyId}/changeRole/{employeeId}")
    public ResponseEntity changeRole(Authentication authentication, @PathVariable Long academyId, @PathVariable Long employeeId) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("🛠 등급 변경를 요청한 사용자 계정 [{}] || 접근하려는 학원 id [{}]", requestAccount, academyId);

        ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(requestAccount, academyId, employeeId);

        return ResponseEntity.ok(Response.success(response));

    }

}
