package com.project.myacademy.domain.file.academyprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyProfileResponse {

    private String uploadFileName;
    private String s3StoredFileName;
    private String message;

    public static CreateAcademyProfileResponse of(String original, String stored) {
        return CreateAcademyProfileResponse.builder()
                .uploadFileName(original)
                .s3StoredFileName(stored)
                .message("파일 첨부 완료")
                .build();
    }
}
