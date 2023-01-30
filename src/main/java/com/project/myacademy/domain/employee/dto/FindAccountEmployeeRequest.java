package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FindAccountEmployeeRequest {
    private String academyName;
    private String name;
    private String email;
}
