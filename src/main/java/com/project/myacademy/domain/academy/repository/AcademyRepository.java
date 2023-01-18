package com.project.myacademy.domain.academy.repository;

import com.project.myacademy.domain.academy.entity.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findByBusinessRegistrationNumber(String businessRegistrationNumber);
}
