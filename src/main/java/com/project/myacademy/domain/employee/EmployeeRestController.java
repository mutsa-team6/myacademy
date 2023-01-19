package com.project.myacademy.domain.employee;

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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @PostMapping("/employees/signup")
    public ResponseEntity create(@RequestBody CreateEmployeeRequest request) {
        EmployeeDto savedEmployeeDto = employeeService.createEmployee(request);
        return ResponseEntity.ok(Response.success(new CreateEmployeeResponse(
                savedEmployeeDto.getName(),
                savedEmployeeDto.getAccount(),
                "signed up")));
    }

    @PostMapping("/employees/login")
    public ResponseEntity login(@RequestBody LoginEmployeeRequest request) {
        LoginEmployeeResponse response = employeeService.loginEmployee(request);
        return ResponseEntity.ok(Response.success(response));
    }

    @PostMapping("/employees/findAccount")
    public ResponseEntity findAccount(@RequestBody FindAccountEmployeeRequest request) {
        FindAccountEmployeeResponse response = employeeService.findAccountEmployee(request);
        return ResponseEntity.ok(Response.success(response));
    }

    @PutMapping("/employees/findPassword")
    public ResponseEntity changePassword(@RequestBody ChangePasswordEmployeeRequest request) {
        EmployeeDto updatedEmployeeDto = employeeService.changePasswordEmployee(request);
        return ResponseEntity.ok(Response.success(new ChangePasswordEmployeeResponse(
                updatedEmployeeDto.getName(),
                updatedEmployeeDto.getAccount(),
                updatedEmployeeDto.getAccount() + "updated")));
    }

    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity delete(@PathVariable Long employeeId) {
        DeleteEmployeeResponse response = employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/employees/{employeeId}/my")
    public ResponseEntity read(@PathVariable Long employeeId) {
        ReadEmployeeResponse response = employeeService.readEmployee(employeeId);
        return ResponseEntity.ok(Response.success(response));
    }

    // 관리자 회원만 접근할 수 있는, 전체 회원 보기
    @GetMapping("/employees")
    public ResponseEntity readAll(Authentication authentication, Pageable pageable) {

        String requestAccount = authentication.getName();
        log.info("🔎 조회를 요청한 사용자 계정 {} ", requestAccount);

        Page<ReadEmployeeResponse> response = employeeService.readAllEmployees(requestAccount, pageable);

        return ResponseEntity.ok(Response.success(response));
    }

}
