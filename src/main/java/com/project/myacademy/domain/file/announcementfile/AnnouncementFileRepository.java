package com.project.myacademy.domain.file.announcementfile;

import com.project.myacademy.domain.file.employeeprofile.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementFileRepository extends JpaRepository<AnnouncementFile, Long> {
    List<AnnouncementFile> findByAnnouncement_Id(Long id);

    Boolean existsAnnouncementFileByAnnouncement_Id(Long announcementId);
}