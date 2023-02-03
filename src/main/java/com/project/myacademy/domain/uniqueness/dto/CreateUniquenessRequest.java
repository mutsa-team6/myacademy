package com.project.myacademy.domain.uniqueness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateUniquenessRequest {
    //특이사항
    private String body;
}
