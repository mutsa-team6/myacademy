package com.project.myacademy.domain.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FindParentForUIResponse {
    private Boolean isExist;
    private Long academyId;
}
