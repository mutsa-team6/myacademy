package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniquenessRepository extends JpaRepository<Uniqueness, Long> {
    Page<Uniqueness> findAllByStudent(Student student, Pageable pageable);
}
