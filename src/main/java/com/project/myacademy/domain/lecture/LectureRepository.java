package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    Optional<Lecture> findByName(String lectureName);

    // 강좌 이름 키워드 검색 페이징
    Page<Lecture> findByNameContaining(String keyword, Pageable pageable);

    Page<Lecture> findByEmployeeAndFinishDateGreaterThanOrderByStartDate(Employee employee, LocalDate finishDate, Pageable pageable);

    Page<Lecture> findByAcademyIdAndFinishDateGreaterThanOrderByCreatedAtDesc(Long academyId, LocalDate finishDate, Pageable pageable);


}
