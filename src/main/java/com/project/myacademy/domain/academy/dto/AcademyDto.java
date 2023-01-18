package com.project.myacademy.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AcademyDto {
    private Long id;
    private String name;
    private String owner;
    private String message;
}
