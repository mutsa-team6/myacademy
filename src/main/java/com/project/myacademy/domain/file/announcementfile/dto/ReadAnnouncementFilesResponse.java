package com.project.myacademy.domain.file.announcementfile.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ReadAnnouncementFilesResponse {
    private String fileUrl;
    private String fileName;
}
