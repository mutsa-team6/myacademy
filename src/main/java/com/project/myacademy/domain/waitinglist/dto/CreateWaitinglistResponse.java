package com.project.myacademy.domain.waitinglist.dto;

import com.project.myacademy.domain.enrollment.dto.CreateEnrollmentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateWaitinglistResponse {

    private Long waitinglistId;
    private String message;

    public static CreateWaitinglistResponse of(Long waitinglistId) {
        return new CreateWaitinglistResponse(waitinglistId, "대기번호 등록 완료");
    }
}