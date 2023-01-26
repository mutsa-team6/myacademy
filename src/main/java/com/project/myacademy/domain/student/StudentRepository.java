package com.project.myacademy.domain.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByPhoneNumAndAcademyId(String phoneNum, Long academyId);

    Page<Student> findAllByAcademyId(Pageable pageable, Long academyId);

    Optional<Student> findByAcademyIdAndId(Long academyId, Long studentId);
}
