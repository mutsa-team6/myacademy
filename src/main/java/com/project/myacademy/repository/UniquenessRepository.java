package com.project.myacademy.repository;

import com.project.myacademy.domain.entity.Uniqueness;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniquenessRepository extends JpaRepository<Uniqueness, Long> {
}
