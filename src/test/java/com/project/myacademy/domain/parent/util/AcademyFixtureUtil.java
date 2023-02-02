package com.project.myacademy.domain.parent.util;

import com.project.myacademy.domain.academy.Academy;

public enum AcademyFixtureUtil {
    ACADEMY1(1L, "name", "address", "phoneNum", "admin", "businessRegistrationNumber");

    private Long id;
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;

    AcademyFixtureUtil(Long id, String name, String address, String phoneNum, String owner, String businessRegistrationNumber) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.owner = owner;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public Academy init() {
        return Academy.builder()
                .id(this.id)
                .name(this.name)
                .address(this.address)
                .phoneNum(this.phoneNum)
                .owner(this.owner)
                .businessRegistrationNumber(this.businessRegistrationNumber)
                .build();
    }
}
