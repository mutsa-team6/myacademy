package com.project.myacademy.domain.enrollment.dto;

import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FindEnrollmentResponse {

    private Long studentId;
    private Long lectureId;
    private String studentName;
    private String studentPhoneNum;
    private String lectureName;
    private String teacherName;
    private Integer price;
    private Integer discount;

    private Boolean paymentYN;
    public FindEnrollmentResponse(Enrollment enrollment) {
        this.studentId = enrollment.getStudent().getId();
        this.lectureId = enrollment.getLecture().getId();
        this.studentName = enrollment.getStudent().getName();
        this.studentPhoneNum = enrollment.getStudent().getPhoneNum();
        this.lectureName = enrollment.getLecture().getName();
        this.price = enrollment.getLecture().getPrice();
        this.discount = enrollment.getLecture().getPrice();
        this.paymentYN = enrollment.getPaymentYN();
        this.teacherName = enrollment.getLecture().getEmployee().getName();
    }
}
