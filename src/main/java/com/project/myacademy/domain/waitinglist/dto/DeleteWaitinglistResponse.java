package com.project.myacademy.domain.waitinglist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class DeleteWaitinglistResponse {

    private Long waitinglistId;
    private String message;

    public static DeleteWaitinglistResponse of(Long waitinglistId) {
        return new DeleteWaitinglistResponse(waitinglistId, "대기번호 삭제 완료");
    }
}