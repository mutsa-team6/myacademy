package com.project.myacademy.repository;

import com.project.myacademy.domain.entity.StudentLecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentLectureRepository extends JpaRepository<StudentLecture, Long> {
}
