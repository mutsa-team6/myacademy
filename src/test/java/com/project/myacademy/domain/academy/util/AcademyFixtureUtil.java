package com.project.myacademy.domain.academy.util;

import com.project.myacademy.domain.academy.entity.Academy;

public enum AcademyFixtureUtil {
    ACADEMY_ADMIN(1L, "name", "address", "phoneNum", "admin", "businessRegistrationNumber", "password");

    private Long id;
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;
    private String password;

    AcademyFixtureUtil(Long id, String name, String address, String phoneNum, String owner, String businessRegistrationNumber, String password) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.owner = owner;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.password = password;
    }

    public Academy init() {
        return Academy.builder()
                .id(this.id)
                .name(this.name)
                .address(this.address)
                .phoneNum(this.phoneNum)
                .owner(this.owner)
                .businessRegistrationNumber(this.businessRegistrationNumber)
                .password(this.password)
                .build();
    }
}
