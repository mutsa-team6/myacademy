package com.project.myacademy.domain.academy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    Optional<Academy> findByName(String academyName);

    Page<Academy> findAll(Pageable pageable);

    boolean existsByName(String academyName);


}
