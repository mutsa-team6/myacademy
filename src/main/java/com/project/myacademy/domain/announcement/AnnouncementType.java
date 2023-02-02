package com.project.myacademy.domain.announcement;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AnnouncementType {
    ADMISSION("입시정보"),
    ANNOUNCEMENT("공지사항");

    private String type;
}
