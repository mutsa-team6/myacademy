package com.project.myacademy.global.configuration.oauth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmployeeProfile {
    //Resource Server마다 제공하는 정보가 다르므로 통일시키기 위한 profile
    private final String name;
    private final String email;

    public EmployeeProfile(String name, String email) {

        this.name = name;
        this.email = email;

    }
}
