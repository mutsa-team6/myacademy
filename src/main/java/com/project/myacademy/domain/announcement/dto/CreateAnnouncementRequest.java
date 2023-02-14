package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAnnouncementRequest {
    //공지사항 제목
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;
    //공자시항 내용
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String body;
    //공지사항 타입
    @NotNull
    private AnnouncementType type;
}
