package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
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

}
