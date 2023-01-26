package com.project.myacademy.domain.payment.dto;

import com.project.myacademy.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseHandleFailDto {
    ErrorCode errorCode;
    String errorMsg;
    String orderId;
}
