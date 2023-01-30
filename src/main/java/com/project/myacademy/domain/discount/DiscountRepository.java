package com.project.myacademy.domain.discount;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByDiscountName(String name);
}