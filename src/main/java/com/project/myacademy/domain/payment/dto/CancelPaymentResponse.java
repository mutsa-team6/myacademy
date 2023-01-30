package com.project.myacademy.domain.payment.dto;

import lombok.Data;

@Data
public class CancelPaymentResponse {
    Long cancelAmount;
    String cancelReason;
    Integer taxFreeAmount;
    Integer taxAmount;
    Integer refundableAmount;
    String canceledAt;
}
