package com.project.myacademy.domain.file.employeeprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateEmployeeProfileResponse {

    private String uploadFileName;
    private String s3StoredFileName;
    private String message;

    public static CreateEmployeeProfileResponse of(String original, String stored) {
        return CreateEmployeeProfileResponse.builder()
                .uploadFileName(original)
                .s3StoredFileName(stored)
                .message("파일 첨부 완료")
                .build();
    }
}