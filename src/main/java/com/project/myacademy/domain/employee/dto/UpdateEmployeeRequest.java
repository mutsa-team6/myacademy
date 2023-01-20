package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UpdateEmployeeRequest {
    private String name;
    private String address;
    private String email;
    private String password;
    private String phoneNum;
}
