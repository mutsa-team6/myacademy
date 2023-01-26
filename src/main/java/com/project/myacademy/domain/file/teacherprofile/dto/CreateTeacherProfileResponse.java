package com.project.myacademy.domain.file.teacherprofile.dto;

import com.project.myacademy.domain.file.teacherprofile.TeacherProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateTeacherProfileResponse {

    private String uploadFileName;
    private String message;

    public static CreateTeacherProfileResponse of(TeacherProfile profileImages) {
        return CreateTeacherProfileResponse.builder()
                .uploadFileName(profileImages.getUploadFileName())
                .message("파일 첨부 완료")
                .build();
    }
}