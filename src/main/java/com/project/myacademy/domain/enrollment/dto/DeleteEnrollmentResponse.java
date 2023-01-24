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
    private Long newEnrollmentId;
    private String message;

    public static DeleteEnrollmentResponse of(Long enrollmentId, Long newEnrollmentId) {
        return new DeleteEnrollmentResponse(enrollmentId, newEnrollmentId, "기존 수강 내역 삭제 성공, 대기번호 -> 수강 내역 이동 성공");
    }
}
