package com.project.myacademy.repository;

import com.project.myacademy.domain.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {
}
