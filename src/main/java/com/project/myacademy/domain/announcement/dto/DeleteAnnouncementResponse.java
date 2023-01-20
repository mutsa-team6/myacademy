package com.project.myacademy.domain.announcement.dto;

import com.project.myacademy.domain.announcement.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeleteAnnouncementResponse {
    //삭제된 공지사항 id
    private Long id;
    //삭제된 공지사항 제목
    private String title;

    public static DeleteAnnouncementResponse of(Announcement announcement) {
        return DeleteAnnouncementResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .build();
    }
}
