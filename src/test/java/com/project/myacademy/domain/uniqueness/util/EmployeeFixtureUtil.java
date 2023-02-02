package com.project.myacademy.domain.uniqueness.util;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;

public enum EmployeeFixtureUtil {
    ROLE_ADMIN(0L, "name", "address", "phoneNum", "email", "admin", "password", EmployeeRole.ROLE_ADMIN),
    ROLE_STAFF1(1L, "name", "address", "phoneNum", "email", "staff1", "password", EmployeeRole.ROLE_STAFF),
    ROLE_STAFF2(2L, "name", "address", "phoneNum", "email", "staff2", "password", EmployeeRole.ROLE_STAFF),
    ROLE_USER1(3L, "name", "address", "phoneNum", "email", "user1", "password", EmployeeRole.ROLE_USER),
    ROLE_USER2(4L, "name", "address", "phoneNum", "email", "user2", "password", EmployeeRole.ROLE_USER);

    private Long id;
    private String name;
    private String address;
    private String phoneNum;
    private String email;
    private String account;
    private String password;
    private EmployeeRole employeeRole;

    EmployeeFixtureUtil(Long id, String name, String address, String phoneNum, String email, String account, String password, EmployeeRole employeeRole) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.email = email;
        this.account = account;
        this.password = password;
        this.employeeRole = employeeRole;
    }

    public Employee init() {
        return Employee.builder()
                .id(id)
                .name(name)
                .address(address)
                .phoneNum(phoneNum)
                .email(email)
                .account(account)
                .password(password)
                .employeeRole(employeeRole)
                .build();
    }
}