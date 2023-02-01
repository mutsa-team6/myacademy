package com.project.myacademy.domain.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CheckDiscountResponse {

    private String message;

    public static CheckDiscountResponse of(CheckDiscountRequest request) {
        return CheckDiscountResponse.builder()
                .message(request.getDiscountName() + "을 적용했습니다.")
                .build();
    }
}
