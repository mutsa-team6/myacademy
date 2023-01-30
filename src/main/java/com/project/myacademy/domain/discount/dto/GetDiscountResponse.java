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
public class GetDiscountResponse {

    private String discountName;
    private Integer discountRate;

    public static GetDiscountResponse of(Discount discount) {
        return GetDiscountResponse.builder()
                .discountName(discount.getDiscountName())
                .discountRate(discount.getDiscountRate())
                .build();
    }
}