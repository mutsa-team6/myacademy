package com.project.myacademy.domain.payment.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.discount.Discount;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.payment.PayType;
import com.project.myacademy.domain.payment.entity.Payment;
import com.project.myacademy.domain.student.Student;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class CreatePaymentRequest {
    //지불방법
    private PayType payType;
    //지불금액
    private Integer amount;
    //주문 상품 이름
    private String orderName;

    private Long lectureId;

    private Long discountId;

    public Payment toEntity(Employee employee, Student student, Enrollment enrollment, Academy academy) {
        return Payment.builder()
                .orderId(UUID.randomUUID().toString())
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .employee(employee)
                .student(student)
                .lecture(enrollment.getLecture())
                .discountId(discountId)
                .academy(academy)
                .build();
    }
}
