package com.project.myacademy.domain.uniqueness.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateUniquenessRequest {
    //특이사항
    @NotBlank(message = "특이사항 내용은 필수 입력 항목입니다.")
    private String body;
}
