package com.project.myacademy.domain.parent.util;

import com.project.myacademy.domain.employee.Employee;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static com.project.myacademy.domain.academy.util.EmployeeFixtureUtil.ROLE_ADMIN;
import static com.project.myacademy.domain.academy.util.EmployeeFixtureUtil.ROLE_USER1;

public enum AuthenticationFixtureUtil {
    EMPLOYEE_AUTHENTICATION(ROLE_USER1.init()),
    ADMIN_AUTHENTICATION(ROLE_ADMIN.init());

    private final Employee employee;

    AuthenticationFixtureUtil(Employee employee) {
        this.employee = employee;
    }

    public Authentication init() {
        return new UsernamePasswordAuthenticationToken(employee, employee.getPassword());
    }
}