package com.project.myacademy.domain.enrollment.dto;

import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.student.Student;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FindStudentInfoFromEnrollmentByLectureResponse {

    private String studentName;
    private Long studentId;
    private String studentPhoneNum;
    private String studentEmail;
    private Boolean paymentYn;
    private Long waitingNum;
    private Long waitingId;
    private Long lectureId;
    private Long enrollmentId;

    public FindStudentInfoFromEnrollmentByLectureResponse(Student student,Long waitingId,Long lectureId) {
        this.studentName = student.getName();
        this.studentId = student.getId();
        this.studentPhoneNum = student.getPhoneNum();
        this.waitingId = waitingId;
        this.lectureId = lectureId;
    }

    public FindStudentInfoFromEnrollmentByLectureResponse(Enrollment enrollment) {
        this.studentName = enrollment.getStudent().getName();
        this.studentPhoneNum = enrollment.getStudent().getPhoneNum();
        this.studentId = enrollment.getStudent().getId();
        this.paymentYn = enrollment.getPaymentYN();
        this.studentEmail = enrollment.getStudent().getEmail();
        this.enrollmentId = enrollment.getId();
    }
}
