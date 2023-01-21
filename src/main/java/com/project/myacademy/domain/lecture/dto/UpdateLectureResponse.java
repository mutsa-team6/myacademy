package com.project.myacademy.domain.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UpdateLectureResponse {

    private Long lectureId;
    private String message;

    public static UpdateLectureResponse of(Long lectureId) {
        return new UpdateLectureResponse(lectureId, "강좌 정보 변경 완료");
    }
}