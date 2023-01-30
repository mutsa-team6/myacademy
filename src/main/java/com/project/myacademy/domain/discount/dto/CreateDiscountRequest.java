package com.project.myacademy.domain.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateDiscountRequest {

    private String discountName;
    private Integer discountRate;
}