package com.project.myacademy.domain.lecture.dto;

import com.project.myacademy.domain.lecture.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateLectureResponse {

    private Long lectureId;
    private String message;

    public static CreateLectureResponse of(Lecture lecture) {
        return new CreateLectureResponse(lecture.getId(), "강좌 등록 완료");
    }
}