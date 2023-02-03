package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentAndLecture(Student student, Lecture lecture);

    // 현재 강좌 등록인원
    Long countByLecture_Id(Long lectureId);

    List<Enrollment> findByStudentOrderByCreatedAtDesc(Student student);

    Page<Enrollment> findByStudentAndPaymentYNIsTrue(Student student, Pageable pageable);

    Page<Enrollment> findAllByAcademyIdAndPaymentYNIsFalseOrderByCreatedAtDesc(Long academyId, Pageable pageable);

    Optional<Enrollment> findByLecture_IdAndStudent_Id(Long lectureId, Long studentId);

    Page<Enrollment> findByLectureAndPaymentYNIsTrue(Lecture lecture, Pageable pageable);
}
