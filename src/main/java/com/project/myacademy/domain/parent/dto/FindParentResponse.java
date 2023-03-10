package com.project.myacademy.domain.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class FindParentResponse {
    private Long parentId;
    private String parentName;
    private String parentPhoneNum;
}
