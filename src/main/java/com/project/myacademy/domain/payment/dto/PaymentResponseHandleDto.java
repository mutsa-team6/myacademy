package com.project.myacademy.domain.payment.dto;

import lombok.Data;

@Data
public class PaymentResponseHandleDto {
    String mId; //가맹점 ID
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
    PaymentResponseHandleCardDto card;

}
