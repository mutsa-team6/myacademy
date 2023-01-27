package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyResponse {
    private Long academyId;
    private String name;
    private String owner;
    private String address;
    private String phoneNum;
    private String businessRegistrationNumber;


    public CreateAcademyResponse(Academy academy) {
        this.academyId = academy.getId();
        this.name = academy.getName();
        this.owner = academy.getOwner();
        this.address = academy.getAddress();
        this.phoneNum = academy.getPhoneNum();
        this.businessRegistrationNumber = academy.getBusinessRegistrationNumber();
    }
}
