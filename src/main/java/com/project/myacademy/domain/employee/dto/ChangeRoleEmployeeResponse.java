package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangeRoleEmployeeResponse {
    private Long employeeId;
    private String message;
}
