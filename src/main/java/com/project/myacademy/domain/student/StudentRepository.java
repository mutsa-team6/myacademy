package com.project.myacademy.domain.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByPhoneNumAndAcademyId(String phoneNum, Long academyId);
    Optional<Student> findByEmailAndAcademyId(String email, Long academyId);

    Page<Student> findAllByAcademyId(Pageable pageable, Long academyId);

    Optional<Student> findByAcademyIdAndId(Long academyId, Long studentId);
    Page<Student> findByAcademyIdAndName(Long academyId, String studentName,Pageable pageable);

    Long countStudentByAcademyId(Long academyId);
}
