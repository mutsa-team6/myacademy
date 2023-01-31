package com.project.myacademy.domain.discount;


import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByDiscountNameAndAcademy(String name, Academy academy);

    Page<Discount> findAllByAcademy(Academy academy, Pageable pageable);
}