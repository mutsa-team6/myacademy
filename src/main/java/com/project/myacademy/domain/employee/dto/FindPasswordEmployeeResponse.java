package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FindPasswordEmployeeResponse {
    private Long id;
    private String name;
    private String account;
    private String email;

    public static FindPasswordEmployeeResponse of(Employee employee) {
        return FindPasswordEmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .account(employee.getAccount())
                .email(employee.getEmail())
                .build();
    }
}
