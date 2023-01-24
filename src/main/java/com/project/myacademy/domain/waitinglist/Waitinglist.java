package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "waiting_list_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE waiting_list_tb SET deleted_at = current_timestamp WHERE waiting_list_id = ?")
public class Waitinglist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_list_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String memo;

    public static Waitinglist makeWaitinglist(Lecture lecture, Student student, CreateWaitinglistRequest request) {
        return Waitinglist.builder()
                .lecture(lecture)
                .student(student)
                .memo(request.getMemo())
                .build();
    }
}
