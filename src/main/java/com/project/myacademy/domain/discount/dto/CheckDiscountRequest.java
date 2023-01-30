package com.project.myacademy.domain.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CheckDiscountRequest {

    private String discountName;
    private Long enrollmentId;
}