package com.project.myacademy.domain.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateEnrollmentRequest {

    private String memo;
    private Boolean paymentYN = false;
    private Long discountId = 0L;
}