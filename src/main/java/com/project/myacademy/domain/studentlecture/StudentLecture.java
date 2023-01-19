package com.project.myacademy.domain.studentlecture;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.studentlecture.dto.CreateStudentLectureRequest;
import com.project.myacademy.domain.studentlecture.dto.UpdateStudentLectureRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "student_lecture_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE student_lecture_tb SET deleted_at = current_timestamp WHERE student_lecture_id = ?")
public class StudentLecture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String memo;

    public static StudentLecture createStudentLecture(Student student, Lecture lecture, Payment payment, CreateStudentLectureRequest request) {
        return StudentLecture.builder()
                .student(student)
                .lecture(lecture)
                .payment(payment)
                .memo(request.getMemo())
                .build();
    }

    public void updateStudentLecture(UpdateStudentLectureRequest request) {
        this.memo = request.getMemo();
    }
}
