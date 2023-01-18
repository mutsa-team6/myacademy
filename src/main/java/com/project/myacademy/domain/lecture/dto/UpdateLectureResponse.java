package com.project.myacademy.domain.lecture.dto;

import com.project.myacademy.domain.lecture.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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