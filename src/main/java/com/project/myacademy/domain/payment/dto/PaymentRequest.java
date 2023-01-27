package com.project.myacademy.domain.payment.dto;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.payment.PayType;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.student.Student;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class PaymentRequest {
    //지불방법
    private PayType payType;
    //지불금액
    private Integer amount;
    //주문 상품 이름
    private String orderName;


    public Payment toEntity(Employee employee, Student student, Enrollment enrollment){
        return Payment.builder()
                .orderId(UUID.randomUUID().toString())
                .payType(payType)
                .amount(amount)
                .orderName(orderName)
                .employee(employee)
                .student(student)
                .lecture(enrollment.getLecture())
                .build();
    }
}
