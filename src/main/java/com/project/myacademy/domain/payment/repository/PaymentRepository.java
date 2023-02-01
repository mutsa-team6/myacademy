package com.project.myacademy.domain.payment.repository;

import com.project.myacademy.domain.payment.entity.Payment;
import com.project.myacademy.domain.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Page<Payment> findByAcademy_IdAndPaymentKeyIsNotNullOrderByCreatedAtDesc(Long academyId, Pageable pageable);
    List<Payment> findByAcademy_IdAndPaymentKeyIsNotNullAndStudentOrderByCreatedAtDesc(Long academyId, Student student);

}
