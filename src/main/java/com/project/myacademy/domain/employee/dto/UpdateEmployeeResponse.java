package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UpdateEmployeeResponse {
    private String name;
    private String account;
    private String message;
}
