package com.project.myacademy.domain.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateEnrollmentResponse {

    private Long enrollmentId;
    private String message;

    public static UpdateEnrollmentResponse of(Long enrollmentId) {
        return new UpdateEnrollmentResponse(enrollmentId, "수강 수정 완료");
    }
}