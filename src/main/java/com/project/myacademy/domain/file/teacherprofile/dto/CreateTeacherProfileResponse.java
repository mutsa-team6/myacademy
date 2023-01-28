package com.project.myacademy.domain.file.teacherprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateTeacherProfileResponse {

    private List<String> uploadFileName;
    private List<String> S3StoredFileName;
    private String message;

    public static CreateTeacherProfileResponse of(List<String> original, List<String> stored ) {
        return CreateTeacherProfileResponse.builder()
                .uploadFileName(original)
                .S3StoredFileName(stored)
                .message("파일 첨부 완료")
                .build();
    }
}