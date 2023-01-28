package com.project.myacademy.domain.file.announcementfile;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.announcement.Announcement;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "announcement_file_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE announcement_file_tb SET deleted_at = current_timestamp WHERE announcement_file_id = ?")
public class AnnouncementFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_file_id")
    private Long id;

    private String uploadFileName;
    private String storedFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    public static AnnouncementFile makeAnnouncementFile(String uploadFileName, String storedFileUrl, Announcement announcement) {
        return AnnouncementFile.builder()
                .uploadFileName(uploadFileName)
                .storedFileUrl(storedFileUrl)
                .announcement(announcement)
                .build();
    }
}