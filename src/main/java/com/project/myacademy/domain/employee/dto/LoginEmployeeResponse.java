package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class LoginEmployeeResponse {
    private String jwt;
    private String employeeName;
}
