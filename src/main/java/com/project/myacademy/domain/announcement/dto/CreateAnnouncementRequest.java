package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateAnnouncementRequest {
    //공지사항 제목
    private String title;
    //공자시항 내용
    private String body;
    //공지사항 타입
    private AnnouncementType type;
}
