package com.project.myacademy.domain.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UpdateTeacherResponse {

    private Long teacherId;
    private String message;

    public static UpdateTeacherResponse of(Long teacherId) {
        return UpdateTeacherResponse.builder()
                .teacherId(teacherId)
                .message("강좌의 강사 정보 수정 완료")
                .build();
    }
}