package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.entity.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyRequest {
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;
    private String password;

    public Academy toAcademy(String password) {
        return Academy.builder()
                .name(this.name)
                .address(this.address)
                .phoneNum(this.phoneNum)
                .owner(this.owner)
                .businessRegistrationNumber(this.businessRegistrationNumber)
                .password(password)
                .build();
    }
}
