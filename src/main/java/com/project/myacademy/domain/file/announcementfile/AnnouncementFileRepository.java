package com.project.myacademy.domain.file.announcementfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementFileRepository extends JpaRepository<AnnouncementFile, Long> {
}