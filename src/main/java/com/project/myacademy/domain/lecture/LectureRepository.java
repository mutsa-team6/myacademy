package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.teacher.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Optional<Lecture> findByName(String lectureName);

    // 강좌 이름 키워드 검색 페이징
    Page<Lecture> findByNameContaining(String keyword, Pageable pageable);

    // 특정 강사의 강좌 페이징
    Page<Lecture> findAllByTeacher(Teacher teacher, Pageable pageable);

}
