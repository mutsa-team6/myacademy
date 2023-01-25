package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WaitinglistRepository extends JpaRepository<Waitinglist, Long> {

    Optional<Waitinglist> findByStudentAndLecture(Student student, Lecture lecture);

    // 해당 강좌에서 제일 먼저 등록된 대기번호 추출 메서드
    Optional<Waitinglist> findTopByLectureOrderByCreatedAtAsc(Lecture lecture);
}
