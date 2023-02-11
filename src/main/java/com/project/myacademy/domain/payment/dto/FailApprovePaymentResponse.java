package com.project.myacademy.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FailApprovePaymentResponse {
    String errorCode;
    String errorMsg;
    String orderId;
}
