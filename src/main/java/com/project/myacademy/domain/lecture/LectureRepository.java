package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Optional<Lecture> findByName(String lectureName);

    // 강좌 이름 키워드 검색 페이징
    Page<Lecture> findByNameContaining(String keyword, Pageable pageable);

    Page<Lecture> findByEmployee(Employee employee, Pageable pageable);



}
