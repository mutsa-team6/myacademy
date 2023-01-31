package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.enrollment.dto.CreateEnrollmentRequest;
import com.project.myacademy.domain.enrollment.dto.UpdateEnrollmentRequest;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
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
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    private String memo;

    @Column(name = "paymentYN")
    private Boolean paymentYN;

    @Column(name = "first_register_employee")
    private String registerEmployee;

    @Column(name = "last_modified_employee")
    private String modifiedEmployee;

    private Long discountId;

    private Long academyId;

    // 수강 생성 메서드
    public static Enrollment createEnrollment(Student student, Lecture lecture, Employee employee,Long academyId) {
        StringBuilder sb = new StringBuilder();
        return Enrollment.builder()
                .student(student)
                .lecture(lecture)
                .memo("")
                .paymentYN(false)
                .registerEmployee(sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString())
                .modifiedEmployee(sb.toString())
                .discountId(0L)
                .academyId(academyId)
                .build();
    }

    // 수강 수정 메서드
    public void updateEnrollment(Employee employee, UpdateEnrollmentRequest request) {
        StringBuilder sb = new StringBuilder();
        this.modifiedEmployee = sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString();
        this.memo = request.getMemo();
    }

    // 수강 삭제 시 해당 작업 진행한 직원 업데이트
    public void recordDeleteEmployee(Employee employee) {
        StringBuilder sb = new StringBuilder();
        this.modifiedEmployee = sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString();
    }

    // paymentYN 수정
    public void updatePaymentYN() {
        this.paymentYN = true;
    }

    // 할인정책 id 수정
    public void updateDiscountId(Long discountId) {
        this.discountId = discountId;
    }
}

