package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        LoginEmployeeResponse response = employeeService.login(request);
        return ResponseEntity.ok(Response.success(response));
    }
    @PostMapping("/employees/findaccount")
    public ResponseEntity read(@RequestBody ReadEmployeeAccountRequest request) {
        ReadEmployeeResponse response = employeeService.readAccount(request);
        return ResponseEntity.ok(Response.success(response));
    }
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity update(@PathVariable Long employeeId, @RequestBody UpdateEmployeeRequest request) {
        EmployeeDto updatedEmployeeDto = employeeService.updateEmployee(employeeId, request);
        return ResponseEntity.ok(Response.success(new UpdateEmployeeResponse(
                updatedEmployeeDto.getName(),
                updatedEmployeeDto.getAccount(),
                updatedEmployeeDto.getAccount()+ "updated")));
    }
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity delete(@PathVariable Long employeeId) {
        DeleteEmployeeResponse response = employeeService.deleteEmployee(employeeId);
        return ResponseEntity.ok(Response.success(response));
    }

    @GetMapping("/employees/{employeeId}/my")
    public ResponseEntity readAll(@PathVariable Long employeeId) {
        ReadAllEmployeeResponse response = employeeService.readAll(employeeId);
        return ResponseEntity.ok(Response.success(response));
    }
}
