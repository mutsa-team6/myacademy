package com.project.myacademy.domain.payment.dto;

import com.project.myacademy.domain.payment.entity.CancelPayment;
import lombok.Data;

@Data
public class ApprovePaymentResponse {
    String version;
    String paymentKey;
    String orderId;
    String orderName;
    String currency;
    String method;
    String totalAmount;
    String balanceAmount;
    String suppliedAmount;
    String vat;
    String status;
    String requestedAt;
    String approvedAt;
    String useEscrow;
    String cultureExpense;
    CancelPaymentResponse[] cancels;

    public CancelPayment toCancelPayment() {
        return CancelPayment.builder()
                .orderId(orderId)
                .orderName(orderName)
                .paymentKey(paymentKey)
                .amount(Math.toIntExact(cancels[0].getCancelAmount()))
                .cancelReason(cancels[0].getCancelReason())
                .build();
    }
}
