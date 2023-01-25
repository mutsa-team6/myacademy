package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class FindAcademyRequest {
    private String name;
}
