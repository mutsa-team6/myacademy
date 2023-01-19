package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateEmployeeRequest {
    private String name;
    private String address;
    private String phoneNum;
    private String email;
    private String account;
    private String password;

    public Employee toEmployee(String account, String encryptedPassword, EmployeeRole role) {
        return Employee.builder()
                .name(this.name)
                .address(this.address)
                .phoneNum(this.phoneNum)
                .email(this.email)
                .employeeRole(role)
                .account(account)
                .password(encryptedPassword)
                .build();
    }
}
