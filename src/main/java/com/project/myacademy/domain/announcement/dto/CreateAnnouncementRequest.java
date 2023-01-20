package com.project.myacademy.domain.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAnnouncementRequest {
    //공지사항 제목
    private String title;
    //공자시항 내용
    private String body;
}
