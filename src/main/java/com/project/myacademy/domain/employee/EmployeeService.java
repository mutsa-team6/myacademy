package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final EmployeeRole DEFAULT_EMPLOYEE_ROLE = EmployeeRole.ROLE_USER;

    @Value("${jwt.token.secret}")
    private String secretKey;
    private long expiredTimeMs = 1000 * 60 * 60;

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        String account = request.getAccount();
        employeeRepository.findByAccount(account)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT, ErrorCode.DUPLICATED_ACCOUNT.getMessage());
                });
        String encryptedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        Employee savedEmployee = employeeRepository.save(request.toEmployee(account, encryptedPassword, DEFAULT_EMPLOYEE_ROLE));
        return savedEmployee.toEmployeeDto();
    }

    public LoginEmployeeResponse login(LoginEmployeeRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();
        Employee foundEmployee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        if (!bCryptPasswordEncoder.matches(password, foundEmployee.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, ErrorCode.INVALID_PASSWORD.getMessage());
        }
        return new LoginEmployeeResponse(JwtTokenUtil.createToken(account, secretKey, expiredTimeMs), "login succeeded");
    }

    public ReadEmployeeResponse readAccount(ReadEmployeeAccountRequest request) {
        String name = request.getName();
        String email = request.getEmail();
        Employee foundEmployee = employeeRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        String account = foundEmployee.getAccount();
        return new ReadEmployeeResponse(account, "Account found : " + account);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long employeeId, UpdateEmployeeRequest request) {
        String account = request.getAccount();
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        employeeRepository.findByAccount(account)
                .ifPresent(employee -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACCOUNT, ErrorCode.DUPLICATED_ACCOUNT.getMessage());
                });
        foundEmployee.update(request);
        Employee updatedEmployee = employeeRepository.save(foundEmployee);
        return updatedEmployee.toEmployeeDto();
    }

    @Transactional
    public DeleteEmployeeResponse deleteEmployee(Long employeeId) {
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));

        employeeRepository.delete(foundEmployee);
        return new DeleteEmployeeResponse(employeeId, "Employee deleted : " + employeeId);
    }


    public ReadAllEmployeeResponse readAll(Long employeeId) {
        Employee foundEmployee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND, ErrorCode.EMPLOYEE_NOT_FOUND.getMessage()));
        return new ReadAllEmployeeResponse(foundEmployee);
    }

    /**
     * JwtTokenFilter 에서 사용하기 위해 만든 메서드 ( 계정 찾아와서 권한 부여하기 위함 )
     */
    public EmployeeRole findRoleByAccount(String account) {
        return employeeRepository.findByAccount(account).get().getEmployeeRole();
    }

}
