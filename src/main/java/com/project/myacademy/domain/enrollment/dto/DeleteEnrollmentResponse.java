package com.project.myacademy.domain.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class DeleteEnrollmentResponse {

    private Long deletedEnrollmentId;
    private String message;

    public static DeleteEnrollmentResponse of(Long enrollmentId) {
        return new DeleteEnrollmentResponse(enrollmentId, "수강 등록 삭제 완료");
    }
}
