package com.project.myacademy.domain.teacher.dto;

import com.project.myacademy.domain.teacher.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateTeacherResponse {

    private Long teacherId;
    private String message;

    public static CreateTeacherResponse of(Teacher teacher) {
        return CreateTeacherResponse.builder()
                .teacherId(teacher.getId())
                .message("강사 등록 완료")
                .build();
    }
}