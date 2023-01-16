package com.project.myacademy.repository;

import com.project.myacademy.domain.entity.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
}
