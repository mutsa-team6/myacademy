package com.project.myacademy.domain.academy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FindAcademyForUIResponse {
    private Boolean isExist;
    private Long academyId;
}
