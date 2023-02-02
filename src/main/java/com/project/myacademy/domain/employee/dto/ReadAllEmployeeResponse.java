package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReadAllEmployeeResponse {
    private Long id;
    private String name;
    private String phoneNum;
    private String email;
    private String address;
    private String account;
    private EmployeeRole employeeRole;
    private String subject;

    public static ReadAllEmployeeResponse of(Employee employee) {
        return ReadAllEmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .phoneNum(employee.getPhoneNum())
                .email(employee.getEmail())
                .address(employee.getAddress())
                .account(employee.getAccount())
                .employeeRole(employee.getEmployeeRole())
                .subject(employee.getSubject())
                .build();
    }
}
