package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangePasswordEmployeeRequest {
    private String oldPassword;
    private String newPassword;
}
