package com.project.myacademy.domain.payment.entity;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "cancel_payment_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE cancel_payment_tb SET deleted_at = current_timestamp WHERE cancel_payment_id = ?")
public class CancelPayment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_payment_id")
    private Long id;

    private String cancelReason;
    private String orderId;
    private String orderName;
    private String paymentKey;
    private Integer amount;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

}
