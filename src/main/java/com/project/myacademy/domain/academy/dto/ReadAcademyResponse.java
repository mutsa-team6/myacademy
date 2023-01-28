package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.Column;

@AllArgsConstructor
@Getter
@Builder
public class ReadAcademyResponse {
    private Long academyId;
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;

    public ReadAcademyResponse(Academy academy) {
        this.academyId = academy.getId();
        this.name = academy.getName();
        this.address = academy.getAddress();
        this.phoneNum = academy.getPhoneNum();
        this.owner = academy.getOwner();
        this.businessRegistrationNumber = academy.getBusinessRegistrationNumber();
    }
}
