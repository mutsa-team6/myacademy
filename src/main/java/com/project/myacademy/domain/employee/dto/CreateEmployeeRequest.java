package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class CreateEmployeeRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotBlank
    private String phoneNum;
    @NotBlank
    private String email;
    @NotBlank
    private String account;
    @NotBlank
    private String password;
    private String employeeType;
    private String subject;

}
