package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
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
    private final AcademyService academyService;
    private final EmployeeService employeeService;
    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;
    private final AnnouncementFileS3UploadService announcementFileS3UploadService;

    @GetMapping("/academy/announcements")
    public String announcement(@RequestParam(required = false) String title, HttpServletRequest request, Authentication authentication, Model model, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);

        String requestAccount = requestEmployee.getAccount();

        // 게시판, 제목 검색하는 경우와, 검색하지 않은 경우에 따라 announcements 값을 넘겨준다.
        if (title != null) {
            Page<ReadAllAnnouncementResponse> announcements = announcementService.searchAnnouncement(academyId, title, pageable, requestAccount);
            model.addAttribute("announcements", announcements);

        } else {
            Page<ReadAllAnnouncementResponse> announcements = announcementService.readAllAnnouncement(academyId, pageable, requestAccount);
            model.addAttribute("announcements", announcements);
        }


        return "announcement/list";
    }

    @GetMapping("/academy/announcements/write")
    public String announcementWrite(HttpServletRequest request, Authentication authentication, Model model) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);

        return "announcement/write";
    }

    @GetMapping("/academy/announcements/detail")
    public String announcementWrite(@RequestParam(required = false) Long announcementNum, HttpServletRequest request, Authentication authentication, Model model) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        // announcementId를 쿼리 파라미터로 받으면, 해당 Id로 게시판 정보를 가져와 상세페이지를 보여준다.
        ReadAnnouncementResponse announcement = announcementService.readAnnouncement(academyId, announcementNum, requestAccount);
        model.addAttribute("announcement", announcement);

        // 해당 게시글 작성자의 프로필 사진을 가져오기 위해 사용
        String image = employeeProfileS3UploadService.getStoredUrl(announcement.getAuthorId());
        model.addAttribute("image", image);

        // 해당 게시글에 관련된 첨부파일이 있다면 가져온다.
        List<ReadAnnouncementFilesResponse> files = announcementFileS3UploadService.getStoredUrls(announcementNum);
        model.addAttribute("files", files);


        return "announcement/detail";
    }

    @GetMapping("/academy/announcements/edit")
    public String announcementEdit(@RequestParam(required = false) Long announcementNum, HttpServletRequest request, Authentication authentication, Model model) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        // 수정 페이지에 기존 항목을 보여주기 위해, 기존에 작성되어있는 announcement의 정보를 가져온다.
        ReadAnnouncementResponse announcement = announcementService.readAnnouncement(academyId, announcementNum, requestAccount);
        model.addAttribute("announcement", announcement);

        return "announcement/edit";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
        model.addAttribute("academy", academy);
        return academy;
    }

    private ReadEmployeeResponse setSessionEmployeeInfo(HttpServletRequest request, Model model, Authentication authentication, Long academyId) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //view 에 회원 계정, 회원 직책 세션에 저장
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionEmployeeNameAndRole(request, employee);
        model.addAttribute("employee", employee);
        return employee;
    }
}
