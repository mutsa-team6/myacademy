package com.project.myacademy.domain.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class DeleteTeacherResponse {

    private Long teacherId;
    private String message;

    public static DeleteTeacherResponse of(Long teacherId) {
        return DeleteTeacherResponse.builder()
                .teacherId(teacherId)
                .message("강사 삭제 완료")
                .build();
    }
}