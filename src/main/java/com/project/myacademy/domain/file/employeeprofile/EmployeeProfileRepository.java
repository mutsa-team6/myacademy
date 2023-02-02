package com.project.myacademy.domain.file.employeeprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {

    Optional<EmployeeProfile> findByEmployee_Id(Long id);

    Boolean existsEmployeeProfileByEmployee_Id(Long employeeId);
}