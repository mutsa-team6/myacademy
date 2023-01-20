package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academies/")
@RequiredArgsConstructor
@Slf4j
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @PostMapping("/{academyId}/employees/signup")
    public ResponseEntity create(@PathVariable Long academyId, @RequestBody CreateEmployeeRequest request) {

        log.info("🙇🏻‍♂️요청한 학원 id [{}] 요청한 사용자 계정 [{}]",academyId,request.getAccount());

        CreateEmployeeResponse response = employeeService.createEmployee(request, academyId);


        return ResponseEntity.ok(Response.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginEmployeeRequest request) {
        LoginEmployeeResponse response = employeeService.loginEmployee(request);
        return ResponseEntity.ok(Response.success(response));
    }

    @PostMapping("/findAccount")
    public ResponseEntity findAccount(@RequestBody FindAccountEmployeeRequest request) {
        FindAccountEmployeeResponse response = employeeService.findAccountEmployee(request);
        return ResponseEntity.ok(Response.success(response));
    }

    @PutMapping("/findPassword")
    public ResponseEntity changePassword(@RequestBody ChangePasswordEmployeeRequest request) {
        EmployeeDto updatedEmployeeDto = employeeService.changePasswordEmployee(request);
        return ResponseEntity.ok(Response.success(new ChangePasswordEmployeeResponse(
                updatedEmployeeDto.getName(),
                updatedEmployeeDto.getAccount(),
                updatedEmployeeDto.getAccount() + "updated")));
    }

    // 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원 계정을 삭제할 수 있다.

    @DeleteMapping("/{employeeId}")
    public ResponseEntity delete(Authentication authentication, @PathVariable Long employeeId) {

        String requestAccount = authentication.getName();
        log.info(" ❌ 삭제를 요청한 사용자 계정 [{}] ", requestAccount);

        DeleteEmployeeResponse response = employeeService.deleteEmployee(requestAccount, employeeId);

        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/{employeeId}/my")
    public ResponseEntity read(@PathVariable Long employeeId) {
        ReadEmployeeResponse response = employeeService.readEmployee(employeeId);
        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자(ADMIN) 회원만 접근할 수 있는, 전체 회원 보기
    @GetMapping("")
    public ResponseEntity readAll(Authentication authentication, Pageable pageable) {

        String requestAccount = authentication.getName();
        log.info("🔎 조회를 요청한 사용자 계정 [{}] ", requestAccount);

        Page<ReadEmployeeResponse> response = employeeService.readAllEmployees(requestAccount, pageable);

        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자(ADMIN) 혹은 직원(STAFF) 등급은 다른 직원의 등급을 USER -> STAFF 혹은 STAFF -> USER 로 변경할 수 있다.
    @PutMapping("/changeRole/{employeeId}")
    public ResponseEntity changeRole(Authentication authentication, @PathVariable Long employeeId) {

        String requestAccount = authentication.getName();
        log.info("🛠 등급 변경를 요청한 사용자 계정 [{}] ", requestAccount);

        ChangeRoleEmployeeResponse response = employeeService.changeRoleEmployee(requestAccount, employeeId);

        return ResponseEntity.ok(Response.success(response));

    }

}
