package com.project.myacademy.domain.enrollment.dto;

import com.project.myacademy.domain.enrollment.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FindStudentInfoFromEnrollmentByLectureResponse {

    private String studentName;
    private Long studentId;
    private String studentPhoneNum;

    public FindStudentInfoFromEnrollmentByLectureResponse(Enrollment enrollment) {
        this.studentName = enrollment.getStudent().getName();
        this.studentPhoneNum = enrollment.getStudent().getPhoneNum();
        this.studentId = enrollment.getStudent().getId();
    }
}
