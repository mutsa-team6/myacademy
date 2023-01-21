package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UpdateAnnouncementResponse {
    //공지사항 id
    private Long id;
    //변경된 공지사항 제목
    private String title;
    //변경된 공지사항 내용
    private String body;

    public static UpdateAnnouncementResponse of(Announcement announcement) {
        return UpdateAnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .body(announcement.getBody())
                .build();
    }
}
