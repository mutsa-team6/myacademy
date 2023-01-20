package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNameAndEmail(String name, String email);

    Optional<Employee> findByName(String name);

    Optional<Employee> findByAccount(String account);

    Optional<Employee> findByAccountAndAcademy(String account, Academy academy);
}
