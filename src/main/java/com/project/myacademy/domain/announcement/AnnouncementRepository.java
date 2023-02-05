package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findAllByAcademyOrderByCreatedAtDesc(Academy academy, Pageable pageable);

    Page<Announcement> findAllByTypeAndAcademy(AnnouncementType type, Academy academy,Pageable pageable);

    List<Announcement> findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType type, Academy academy);
    Page<Announcement> findAllByAcademyAndTitleContainingOrderByCreatedAtDesc(Academy academy, String title,Pageable pageable);

}
