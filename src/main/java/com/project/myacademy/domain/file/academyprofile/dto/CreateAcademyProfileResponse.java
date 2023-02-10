package com.project.myacademy.domain.file.academyprofile.dto;

import com.project.myacademy.domain.file.employeeprofile.dto.CreateEmployeeProfileResponse;
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
    private String S3StoredFileName;
    private String message;

    public static CreateAcademyProfileResponse of(String original, String stored) {
        return CreateAcademyProfileResponse.builder()
                .uploadFileName(original)
                .S3StoredFileName(stored)
                .message("파일 첨부 완료")
                .build();
    }
}
