package com.project.myacademy.controller;

import com.project.myacademy.domain.announcement.AnnouncementService;
import com.project.myacademy.domain.announcement.dto.ReadAllAnnouncementResponse;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.file.announcementfile.AnnouncementFileS3UploadService;
import com.project.myacademy.domain.file.announcementfile.dto.ReadAnnouncementFilesResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.global.util.AuthenticationUtil;
import com.project.myacademy.global.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final EmployeeService employeeService;
    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;
    private final AnnouncementFileS3UploadService announcementFileS3UploadService;

    @GetMapping("/academy/announcements")
    public String announcement(@RequestParam(required = false) String title, HttpServletRequest request, Authentication authentication, Model model, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        if (title != null) {
            Page<ReadAllAnnouncementResponse> announcements = announcementService.searchAnnouncement(academyId, title, pageable, requestAccount);
            model.addAttribute("announcements", announcements);

        } else {
            Page<ReadAllAnnouncementResponse> announcements = announcementService.readAllAnnouncement(academyId, pageable, requestAccount);
            model.addAttribute("announcements", announcements);
        }

        model.addAttribute("account", requestAccount);
        return "announcement/list";
    }

    @GetMapping("/academy/announcements/write")
    public String announcementWrite(HttpServletRequest request, Authentication authentication, Model model) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        model.addAttribute("academyId", academyId);
        model.addAttribute("account", requestAccount);

        return "announcement/write";
    }

    @GetMapping("/academy/announcements/detail")
    public String announcementWrite(@RequestParam(required = false) Long announcementNum, HttpServletRequest request, Authentication authentication, Model model) {
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);


        ReadAnnouncementResponse announcement = announcementService.readAnnouncement(academyId, announcementNum, requestAccount);
        String image = employeeProfileS3UploadService.getStoredUrl(announcement.getAuthorId());

        List<ReadAnnouncementFilesResponse> files = announcementFileS3UploadService.getStoredUrls(announcementNum);


        model.addAttribute("files", files);
        model.addAttribute("image", image);
        model.addAttribute("academyId", academyId);
        model.addAttribute("announcement", announcement);
        model.addAttribute("account", requestAccount);

        return "announcement/detail";
    }

    @GetMapping("/academy/announcements/edit")
    public String announcementEdit(@RequestParam(required = false) Long announcementNum, HttpServletRequest request, Authentication authentication, Model model) {
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);


        ReadAnnouncementResponse announcement = announcementService.readAnnouncement(academyId, announcementNum, requestAccount);
        String image = employeeProfileS3UploadService.getStoredUrl(announcement.getAuthorId());

        model.addAttribute("image", image);
        model.addAttribute("academyId", academyId);
        model.addAttribute("announcement", announcement);
        model.addAttribute("account", requestAccount);

        return "announcement/edit";
    }
}
