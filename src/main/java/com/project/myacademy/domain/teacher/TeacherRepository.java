package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query(value = "SELECT t from Teacher t WHERE t.name = :name",
            countQuery = "SELECT COUNT(t) FROM Teacher t WHERE t.name = :name")
    Page<Teacher> findByName(String name, Pageable pageable);

    Optional<Teacher> findByEmployee(Employee employee);

    Page<Teacher> findAll(Academy academy, Pageable pageable);
}
