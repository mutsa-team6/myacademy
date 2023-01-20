package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateEmployeeResponse {
    private String name;
    private String account;
    private String academyName;

    public CreateEmployeeResponse(Employee employee, String academyName) {
        this.name = employee.getName();
        this.account = employee.getAccount();
        this.academyName = academyName;
    }
}
