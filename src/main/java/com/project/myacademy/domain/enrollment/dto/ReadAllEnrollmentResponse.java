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
public class ReadAllEnrollmentResponse {

    private Long enrollmentId;
    private String lectureName;
    private Integer lecturePrice;
    private String studentName;
    private String memo;

    public static ReadAllEnrollmentResponse of(Enrollment enrollment) {
        return ReadAllEnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .lectureName(enrollment.getLecture().getName())
                .lecturePrice(enrollment.getLecture().getPrice())
                .studentName(enrollment.getStudent().getName())
                .memo(enrollment.getMemo())
                .build();
    }
}