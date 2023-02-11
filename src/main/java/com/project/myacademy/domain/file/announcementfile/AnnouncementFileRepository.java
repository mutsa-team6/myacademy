package com.project.myacademy.domain.file.announcementfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementFileRepository extends JpaRepository<AnnouncementFile, Long> {
    List<AnnouncementFile> findByAnnouncement_Id(Long id);

    Boolean existsAnnouncementFileByAnnouncement_Id(Long announcementId);
}