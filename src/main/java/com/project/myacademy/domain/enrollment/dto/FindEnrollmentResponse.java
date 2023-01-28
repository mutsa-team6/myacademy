package com.project.myacademy.domain.enrollment.dto;

import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.teacher.Teacher;
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
    public FindEnrollmentResponse(Student student, Lecture lecture, Teacher teacher, Enrollment enrollment) {
        this.studentId = student.getId();
        this.lectureId = lecture.getId();
        this.studentName = student.getName();
        this.studentPhoneNum = student.getPhoneNum();
        this.lectureName = lecture.getName();
        this.teacherName = teacher.getName();
        this.price = lecture.getPrice();
        this.discount = lecture.getPrice();
        this.paymentYN = enrollment.getPaymentYN();
    }
}
