package com.project.myacademy.domain.payment.repository;

import com.project.myacademy.domain.payment.entity.CancelPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelPaymentRepository extends JpaRepository<CancelPayment, Long> {
}
