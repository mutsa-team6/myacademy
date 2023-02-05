package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.Announcement;
import com.project.myacademy.domain.announcement.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class ReadAnnouncementResponse {
    //공지사항 id
    private Long id;
    //공지사항 타입
    private String type;
    //공지사항 제목
    private String title;
    //공지사항 내용
    private String body;
    private String author;
    private String createdAt;
    private Long authorId;


    public static ReadAnnouncementResponse of(Announcement announcement) {
        return ReadAnnouncementResponse.builder()
                .id(announcement.getId())
                .type(announcement.getType().getType())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .author(announcement.getEmployee().getName())
                .authorId(announcement.getEmployee().getId())
                .createdAt(new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Timestamp.valueOf(announcement.getCreatedAt())))
                .build();
    }
}
