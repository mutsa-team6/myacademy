package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.Announcement;
import com.project.myacademy.domain.announcement.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Builder
@AllArgsConstructor
@Getter
public class ReadAllAnnouncementResponse {
    //공지사항 id
    private Long id;
    //공지사항 타입
    private String type;
    //공지사항 제목
    private String title;
    //공지사항 내용
    private String body;
    private String createdAt;
    private String author;

    public static ReadAllAnnouncementResponse of(Announcement announcement) {
        return ReadAllAnnouncementResponse.builder()
                .id(announcement.getId())
                .type(announcement.getType().getType())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .author(announcement.getAuthor())
                .createdAt(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Timestamp.valueOf(announcement.getCreatedAt())))
                .build();
    }
}
