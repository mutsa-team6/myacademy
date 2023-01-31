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

    private Long discountId;
    private String discountName;
    private Integer discountRate;

    public static GetDiscountResponse of(Discount discount) {
        return GetDiscountResponse.builder()
                .discountId(discount.getId())
                .discountName(discount.getDiscountName())
                .discountRate(discount.getDiscountRate())
                .build();
    }
}