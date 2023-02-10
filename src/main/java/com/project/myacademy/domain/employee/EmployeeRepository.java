package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNameAndEmail(String name, String email);
    Optional<Employee> findByName(String name);
    Optional<Employee> findByAccount(String account);
    Optional<Employee> findByAccountAndEmail(String account, String email);
    Optional<Employee> findByAccountAndAcademy(String account, Academy academy);
    Optional<Employee> findByIdAndAcademy(Long employeeId, Academy academy);
    @Query("select e from Employee e where e.academy = :academy and not e.account = 'admin'")
    Page<Employee> findAllEmployee(@Param("academy") Academy academy, Pageable pageable);
    @Query("select e from Employee e where e.academy = :academy and not e.subject = '직원'")
    Page<Employee> findAllTeacher(@Param("academy") Academy academy, Pageable pageable);

    Optional<Employee> findByEmail(String requestEmployeeEmail);

    Long countByAcademy(Academy academy);
}
