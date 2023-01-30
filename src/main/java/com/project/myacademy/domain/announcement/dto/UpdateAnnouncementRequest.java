package com.project.myacademy.domain.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateAnnouncementRequest {
    //변경할 공지사항 제목
    private String title;
    //변경할 공지사항 내용
    private String body;
}
