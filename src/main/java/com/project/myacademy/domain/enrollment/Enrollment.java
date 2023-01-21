package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.enrollment.dto.CreateEnrollmentRequest;
import com.project.myacademy.domain.enrollment.dto.UpdateEnrollmentRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "enrollment_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE enrollment_tb SET deleted_at = current_timestamp WHERE enrollment_id = ?")
public class Enrollment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String memo;
    private Integer paymentYN;

    @Column(name = "employee_id")
    private Long employeeId;

    public static Enrollment createEnrollment(Student student, Lecture lecture, CreateEnrollmentRequest request) {
        return Enrollment.builder()
                .student(student)
                .lecture(lecture)
                .memo(request.getMemo())
                .paymentYN(request.getPaymentYN())
                .employeeId(request.getEmployeeId())
                .build();
    }

    public void updateEnrollment(UpdateEnrollmentRequest request) {
        this.memo = request.getMemo();
    }
}
