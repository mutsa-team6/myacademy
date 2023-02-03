package com.project.myacademy.controller;

import com.project.myacademy.domain.announcement.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
}
