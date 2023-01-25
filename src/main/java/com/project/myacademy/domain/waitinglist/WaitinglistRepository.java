package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaitinglistRepository extends JpaRepository<Waitinglist, Long> {

    Optional<Waitinglist> findByStudentAndLecture(Student student, Lecture lecture);
}
