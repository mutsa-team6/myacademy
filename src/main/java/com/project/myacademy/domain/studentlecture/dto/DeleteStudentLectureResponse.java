package com.project.myacademy.domain.studentlecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class DeleteStudentLectureResponse {

    private Long studentLectureId;
    private String message;

    public static DeleteStudentLectureResponse of(Long studentLectureId) {
        return new DeleteStudentLectureResponse(studentLectureId, "학생-수강 삭제 완료");
    }
}
