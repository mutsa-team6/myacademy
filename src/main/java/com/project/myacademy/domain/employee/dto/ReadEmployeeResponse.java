package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class ReadEmployeeResponse {
    private Long id;
    private String name;
    private String address;
    private String email;
    private String account;
    private EmployeeRole employeeRole;
    private Academy academy;
    private String subject;
    private String phoneNum;
    private String imageUrl;

    public ReadEmployeeResponse(Employee foundEmployee) {
        this.id = foundEmployee.getId();
        this.name = foundEmployee.getName();
        this.address = foundEmployee.getAddress();
        this.email = foundEmployee.getEmail();
        this.account = foundEmployee.getAccount();
        this.employeeRole = foundEmployee.getEmployeeRole();
        this.subject = foundEmployee.getSubject();
        this.academy = foundEmployee.getAcademy();
        this.phoneNum = foundEmployee.getPhoneNum();
    }
}
