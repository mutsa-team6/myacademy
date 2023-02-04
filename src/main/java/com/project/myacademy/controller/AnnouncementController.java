package com.project.myacademy.controller;

import com.project.myacademy.domain.announcement.AnnouncementService;
import com.project.myacademy.domain.announcement.dto.ReadAllAnnouncementResponse;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
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

@Controller
@Slf4j
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final EmployeeService employeeService;

    @GetMapping("/academy/announcements")
    public String announcement(@RequestParam(required = false) String announcementName, HttpServletRequest request, Authentication authentication,Model model, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        Page<ReadAllAnnouncementResponse> announcements = announcementService.readAllAnnouncement(academyId, pageable, requestAccount);
        model.addAttribute("announcements", announcements);

        return "announcement/list";
    }

    @GetMapping("/academy/announcements/write")
    public String announcementWrite(@RequestParam(required = false) String announcementName, HttpServletRequest request, Authentication authentication, Model model) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        model.addAttribute("academyId", academyId);

        return "announcement/write";
    }
    @GetMapping("/academy/announcements/detail")
    public String announcementWrite(@RequestParam(required = false) Long announcementsNum, HttpServletRequest request, Authentication authentication, Model model) {
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        ReadAnnouncementResponse announcement = announcementService.readAnnouncement(academyId, announcementsNum, requestAccount);

        model.addAttribute("academyId", academyId);
        model.addAttribute("announcement", announcement);

        return "announcement/detail";
    }
}
