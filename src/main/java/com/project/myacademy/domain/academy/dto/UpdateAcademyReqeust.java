package com.project.myacademy.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UpdateAcademyReqeust {
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;
    private String password;
}
