package com.project.myacademy.domain.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateEnrollmentResponse {

    private Long enrollmentIdId;
    private String message;

    public static CreateEnrollmentResponse of(Long enrollmentIdId) {
        return new CreateEnrollmentResponse(enrollmentIdId, "수강 등록 완료");
    }
}