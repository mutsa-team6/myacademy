package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.announcement.AnnouncementService;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import com.project.myacademy.global.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final EmployeeService employeeService;
    private final AcademyService academyService;
    private final AnnouncementService announcementService;

    @GetMapping("/academy/main")
    public String main(HttpServletRequest request, Model model, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("⭐ 메인 요청한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);


        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request,employee);

        List<ReadAnnouncementResponse> announcements = announcementService.readAnnouncementForMain(academyId, requestAccount);
        List<ReadAnnouncementResponse> admissions = announcementService.readAdmissionForMain(academyId, requestAccount);


        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("announcements", announcements);
        model.addAttribute("admissions", admissions);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);

        return "pages/main";
    }


}
