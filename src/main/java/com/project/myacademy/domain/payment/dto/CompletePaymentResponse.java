package com.project.myacademy.domain.payment.dto;

import com.project.myacademy.domain.payment.PayType;
import com.project.myacademy.domain.payment.entity.CancelPayment;
import com.project.myacademy.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CompletePaymentResponse {

    private PayType payType; //지불방법

    private Integer amount; //지불금액

    private String orderId; //주문 Id
    private Long paymentId; //결제 기본키 Id

    private String orderName; //주문 상품 이름

    private String studentEmail; //학생이메일

    private String studentName; //학생 이름
    private String studentPhoneNum; //학생 전화 번호

    private String discountName; //할인 정책 이름

    private String createdAt; //결제 날짜
    private String deletedAt = " "; //결제 취소 날짜

    private String employeeName; // 결제한 직원 이름
    private Long studentId; // 결제한 학생 이름e

    private Long lectureId; // 결제한 강의 이름
    private String paymentKey; // 결제한 강의 이름

    public CompletePaymentResponse(Payment payment) {
        this.paymentId = payment.getId();
        this.payType = payment.getPayType();
        this.amount = payment.getAmount();
        this.orderId = payment.getOrderId();
        this.orderName = payment.getOrderName();
        this.studentEmail = payment.getStudent().getEmail();
        this.studentName = payment.getStudent().getName();
        this.discountName = "할인 정책 선택 안함.";
        this.createdAt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Timestamp.valueOf(payment.getCreatedAt()));
        this.deletedAt = "";
        this.employeeName = payment.getEmployee().getName();
        this.studentPhoneNum = payment.getStudent().getPhoneNum();
        this.studentId = payment.getStudent().getId();
        this.lectureId = payment.getLecture().getId();
        this.paymentKey = payment.getPaymentKey();
    }

    public void setDiscountName(String discountName) {
        this.discountName = discountName;
    }

    public void setDeletedAt(CancelPayment cancelPayment) {

        this.deletedAt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Timestamp.valueOf(cancelPayment.getCreatedAt()));


    }
}
