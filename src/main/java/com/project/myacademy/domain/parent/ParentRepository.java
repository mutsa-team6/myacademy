package com.project.myacademy.domain.parent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByPhoneNum(String phoneNum);
}
