package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class EmployeeDto {
    private Long id;
    private String name;
    private String address;
    private String email;
    private String account;
    private EmployeeRole employeeRole;
    private String password;
}
