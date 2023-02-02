package com.project.myacademy.domain.parent.util;

import com.project.myacademy.domain.parent.Parent;

public enum ParentFixtureUtil {
    PARENT1(1L, 1L, "name", "phoneNum", "address"),
    PARENT2(2L, 1L, "name", "phoneNum", "address");

    private Long id;
    private Long academyId;
    private String name;
    private String phoneNum;
    private String address;

    ParentFixtureUtil(Long id, Long academyId, String name, String phoneNum, String address) {
        this.id = id;
        this.academyId = academyId;
        this.name = name;
        this.phoneNum = phoneNum;
        this.address = address;
    }

    public Parent init() {
        return Parent.builder()
                .id(id)
                .academyId(academyId)
                .name(name)
                .phoneNum(phoneNum)
                .address(address)
                .build();
    }
}
