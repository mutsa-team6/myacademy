package com.project.myacademy.domain.discount.dto;

import com.project.myacademy.domain.discount.Discount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateDiscountResponse {

    private Long discountId;
    private String message;

    public static CreateDiscountResponse of(Discount discount) {
        return CreateDiscountResponse.builder()
                .discountId(discount.getId())
                .message("할인정책 등록 성공")
                .build();
    }

}