package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FindAcademyRequest {
    private String name;
    private String businessRegistrationNumber;
}
