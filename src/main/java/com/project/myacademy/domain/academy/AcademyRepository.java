package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    Optional<Academy> findByName(String academyName);


}
