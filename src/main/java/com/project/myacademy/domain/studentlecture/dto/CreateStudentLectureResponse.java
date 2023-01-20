package com.project.myacademy.domain.studentlecture.dto;

import com.project.myacademy.domain.studentlecture.StudentLecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateStudentLectureResponse {

    private Long studentLectureId;
    private String message;

    public static CreateStudentLectureResponse of(Long studentLectureId) {
        return new CreateStudentLectureResponse(studentLectureId, "학생-수강 등록 완료");
    }
}