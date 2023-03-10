package com.project.myacademy.domain.enrollment.dto;

import com.project.myacademy.domain.enrollment.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FindEnrollmentResponse {

    private Long enrollmentId;
    private Long studentId;
    private Long lectureId;
    private String studentName;
    private String studentEmail;
    private String studentPhoneNum;
    private String lectureName;
    private String teacherName;
    private Integer price;
    private Integer discount;

    private Boolean paymentYN;
    private String createdAt;
    private String lectureTime;
    private LocalDate startDate;
    private LocalDate finishDate;
    public FindEnrollmentResponse(Enrollment enrollment) {
        this.enrollmentId = enrollment.getId();
        this.studentId = enrollment.getStudent().getId();
        this.lectureId = enrollment.getLecture().getId();
        this.studentName = enrollment.getStudent().getName();
        this.studentEmail = enrollment.getStudent().getEmail();
        this.studentPhoneNum = enrollment.getStudent().getPhoneNum();
        this.lectureName = enrollment.getLecture().getName();
        this.price = enrollment.getLecture().getPrice();
        this.discount = enrollment.getLecture().getPrice();
        this.lectureTime = enrollment.getLecture().getLectureDay() + " // " + enrollment.getLecture().getLectureTime();
        this.paymentYN = enrollment.getPaymentYN();
        this.teacherName = enrollment.getLecture().getEmployee().getName();
        this.createdAt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Timestamp.valueOf(enrollment.getCreatedAt()));
        this.startDate = enrollment.getLecture().getStartDate();
        this.finishDate = enrollment.getLecture().getFinishDate();
    }
}
