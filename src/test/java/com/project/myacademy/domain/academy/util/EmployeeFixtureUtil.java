package com.project.myacademy.domain.academy.util;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;

public enum EmployeeFixtureUtil {
    ROLE_ADMIN(0L, "admin", "password", EmployeeRole.ROLE_ADMIN),
    ROLE_STAFF1(1L, "staff1", "password", EmployeeRole.ROLE_STAFF),
    ROLE_STAFF2(2L, "staff2", "password", EmployeeRole.ROLE_STAFF),
    ROLE_USER1(3L, "user1", "password", EmployeeRole.ROLE_USER),
    ROLE_USER2(4L, "user2", "password", EmployeeRole.ROLE_USER);

    private Long id;
    private String name;
    private String password;
    private EmployeeRole employeeRole;

    EmployeeFixtureUtil(Long id, String name, String password, EmployeeRole employeeRole) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.employeeRole = employeeRole;
    }

    public Employee init() {
        return Employee.builder()
                .id(id)
                .name(name)
                .password(password)
                .employeeRole(employeeRole)
                .build();
    }
}