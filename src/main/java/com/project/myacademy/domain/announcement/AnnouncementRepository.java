package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.academy.Academy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findAllByAcademy(Academy academy, Pageable pageable);

    Page<Announcement> findAllByTypeAndAcademy(AnnouncementType type, Academy academy,Pageable pageable);
}
