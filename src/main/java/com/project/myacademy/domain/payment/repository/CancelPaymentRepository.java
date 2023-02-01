package com.project.myacademy.domain.payment.repository;

import com.project.myacademy.domain.payment.entity.CancelPayment;
import com.project.myacademy.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {

    Optional<CancelPayment> findByPayment(Payment payment);
}
