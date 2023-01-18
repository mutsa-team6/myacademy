package com.project.myacademy.domain.lecture.dto;

import com.project.myacademy.domain.lecture.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
public class DeleteLectureResponse {

    private Long lectureId;
    private String message;

    public static DeleteLectureResponse of(Long lectureId) {
        return new DeleteLectureResponse(lectureId, "강좌 정보 삭제 완료");
    }
}