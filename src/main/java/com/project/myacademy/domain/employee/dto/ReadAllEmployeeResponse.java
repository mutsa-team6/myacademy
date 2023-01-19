package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ReadAllEmployeeResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private String account;
    private EmployeeRole employeeRole;
    private Academy academy;

    public ReadAllEmployeeResponse(Employee foundEmployee) {
        this.id = foundEmployee.getId();
        this.name = foundEmployee.getName();
        this.address = foundEmployee.getAddress();
        this.email = foundEmployee.getEmail();
        this.account = foundEmployee.getAccount();
        this.employeeRole = foundEmployee.getEmployeeRole();
    }
}
