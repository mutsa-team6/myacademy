package com.project.myacademy.domain.payment.dto;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.payment.PayType;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PaymentRequest {
    //지불방법
    private PayType payType;
    //지불금액
    private Long amount;
    //주문 상품 이름
    private String orderName;
//    //구매자 이메일
//    private String studentEmail;
//    //구매자 이름
//    private String studentName;

    public Payment toEntity(Employee employee, Student student){
        return Payment.builder()
                .orderId(UUID.randomUUID().toString())
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .employee(employee)
                .student(student)
                .build();
    }
}
