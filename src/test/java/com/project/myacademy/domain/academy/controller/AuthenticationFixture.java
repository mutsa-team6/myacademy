package com.project.myacademy.domain.academy.controller;

import com.project.myacademy.domain.employee.Employee;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static com.project.myacademy.domain.academy.controller.EmployeeFixture.ROLE_ADMIN;
import static com.project.myacademy.domain.academy.controller.EmployeeFixture.ROLE_USER1;

public enum AuthenticationFixture {
    EMPLOYEE_AUTHENTICATION(ROLE_USER1.init()),
    ADMIN_AUTHENTICATION(ROLE_ADMIN.init());

    private final Employee employee;

    AuthenticationFixture(Employee employee) {
        this.employee = employee;
    }

    public Authentication init() {
        return new UsernamePasswordAuthenticationToken(employee, employee.getPassword());
    }
}