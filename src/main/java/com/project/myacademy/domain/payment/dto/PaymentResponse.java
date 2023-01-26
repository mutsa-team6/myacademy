package com.project.myacademy.domain.payment.dto;

import com.project.myacademy.domain.payment.PayType;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PaymentResponse {
    private PayType payType; //지불방법
    private Long amount; //지불금액
    private String orderId; //주문 Id
    private String orderName; //주문 상품 이름
    private String studentEmail; //학생이메일
    private String studentName; //학생 이름
    private String paySuccessYn; //결제 성공 여부
    private LocalDateTime createdAt; //결제 날짜
    private String successUrl; //성공시 주소
    private String failUrl; //실패시 주소
}
