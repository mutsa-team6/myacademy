package com.project.myacademy.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.myacademy.domain.payment.PayType;
import com.project.myacademy.domain.payment.entity.Payment;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreatePaymentResponse {
    private PayType payType; //지불방법
    private Integer amount; //지불금액
    private String orderId; //주문 Id
    private String orderName; //주문 상품 이름
    private String studentEmail; //학생이메일
    private String studentName; //학생 이름

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt; //결제 날짜

    private String successUrl; //성공시 주소
    private String failUrl; //실패시 주소

    public static CreatePaymentResponse of(Payment payment) {
        return CreatePaymentResponse.builder()
                .payType(payment.getPayType())
                .amount(payment.getAmount())
                .orderId(payment.getOrderId())
                .orderName(payment.getOrderName())
                .studentName(payment.getStudent().getName())
                .studentEmail(payment.getStudent().getEmail())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
