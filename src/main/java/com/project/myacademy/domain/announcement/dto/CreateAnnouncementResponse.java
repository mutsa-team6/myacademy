package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.Announcement;
import com.project.myacademy.domain.announcement.AnnouncementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateAnnouncementResponse {
    //공지사항 id
    private Long id;
    //공지사항 타입
    private AnnouncementType type;
    //공지사항 제목
    private String title;
    //공지사항 내용
    private String body;

    public static CreateAnnouncementResponse of(Announcement announcement) {
        return CreateAnnouncementResponse.builder()
                .id(announcement.getId())
                .type(announcement.getType())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .build();
    }
}
