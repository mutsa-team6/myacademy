package com.project.myacademy.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyRequest {
    private String name;
    private String address;
    private String phoneNum;
    private String owner;
    private String businessRegistrationNumber;
}
