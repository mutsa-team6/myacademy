package com.project.myacademy.domain.studentlecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateStudentLectureResponse {

    private Long studentLectureId;
    private String message;

    public static UpdateStudentLectureResponse of(Long studentLectureId) {
        return new UpdateStudentLectureResponse(studentLectureId, "학생-수강 등록 완료");
    }
}