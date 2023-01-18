package com.project.myacademy.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyResponse {
    private Long id;
    private String name;
    private String owner;
    private String message;
}
