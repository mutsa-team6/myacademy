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
public class GetAppliedDiscountResponse {

    private String discountName;
    private Integer discountRate;

    public static GetAppliedDiscountResponse of(Discount discount) {
        return GetAppliedDiscountResponse.builder()
                .discountName(discount.getDiscountName())
                .discountRate(discount.getDiscountRate())
                .build();
    }
}
