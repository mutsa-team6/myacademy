package com.project.myacademy.domain.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateDiscountRequest {

    @NotBlank(message = "할인 정책 이름은 필수 입력 항목입니다.")
    private String discountName;

    @NotNull(message = "할인율은 필수 입력 항목입니다.")
    @Range(min = 1,max = 99,message = "할인율은 1 ~ 99 사이의 숫자만 입력 가능합니다.")
    private Integer discountRate;
}