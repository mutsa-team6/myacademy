package com.project.myacademy.domain.payment.repository;

import com.project.myacademy.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);
}
