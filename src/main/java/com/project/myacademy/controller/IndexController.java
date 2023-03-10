package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.dto.CreateAcademyResponse;
import com.project.myacademy.domain.academy.dto.FindAcademyRequest;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.announcement.AnnouncementService;
import com.project.myacademy.domain.announcement.AnnouncementType;
import com.project.myacademy.domain.announcement.dto.CreateAnnouncementRequest;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.*;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.file.academyprofile.AcademyProfileS3UploadService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.student.StudentService;
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
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final EmployeeService employeeService;
    private final AcademyService academyService;
    private final AnnouncementService announcementService;
    private final LectureService lectureService;

    private final StudentService studentService;
    private final EnrollmentService enrollmentService;
    private final AcademyProfileS3UploadService academyProfileS3UploadService;

    @GetMapping("/academy/main")
    public String main(HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        List<ReadAnnouncementResponse> announcements = announcementService.readAnnouncementForMain(academyId, requestAccount);
        model.addAttribute("announcements", announcements);


        List<ReadAnnouncementResponse> admissions = announcementService.readAdmissionForMain(academyId, requestAccount);
        model.addAttribute("admissions", admissions);

        Long numberOfEmployees = employeeService.countEmployeesByAcademy(academyId);
        model.addAttribute("numberOfEmployees", numberOfEmployees);

        Long numberOfStudents = studentService.countStudentByAcademy(academyId);
        model.addAttribute("numberOfStudents", numberOfStudents);

        List<ReadAllLectureResponse> lectures = lectureService.readAllTodayLectures(academyId, requestAccount, pageable);
        model.addAttribute("lectures", lectures);

        String imageUrl = academyProfileS3UploadService.getStoredUrl(academyId);
        model.addAttribute("imageUrl", imageUrl);


        // ?????? ???????????? ????????? ?????? ?????? ????????? ????????????.
        // ?????? ????????? ?????? ????????? ?????????, ?????? ????????? ???????????????, ?????? ????????? ??????????????? ???????????? ??????
        for (ReadAllLectureResponse lecture : lectures) {
            lecture.setRegisteredStudent(enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academyId, requestAccount, lecture.getLectureId()));
        }
        model.addAttribute("lectures", lectures);


        return "pages/main";
    }

    @GetMapping("/about")
    public String about() {
        return "about/about";
    }

    @GetMapping("/tester")
    public String testerLogin(HttpServletResponse httpServletResponse) {
        Long academyId;
        String testerEmail = "tester@gmail.com";
        // ?????? ?????? (???????????? ?????? ???????????? ?????? ?????? ??????)
        if (!academyService.checkExistByAcademyName("???????????? ??????")) {
            CreateAcademyResponse academy = academyService.createAcademy(new CreateAcademyRequest("???????????? ??????", "OO ?????? ??????", "010-0000-0000", "?????????", "12-345-67890"));
            academyId = academy.getAcademyId();
        } else {
            FindAcademyResponse academy = academyService.findAcademy(new FindAcademyRequest("???????????? ??????"));
            academyId = academy.getAcademyId();
        }

        // ?????? ????????? ???????????? ?????????????????? ?????? ?????? ??????, ?????? ?????? ????????? ??????
        if (!employeeService.checkExistByEmployeeEmail(testerEmail)) {
            CreateEmployeeResponse employee = employeeService.createEmployee(new CreateEmployeeRequest("?????????", "00??? 00??? 00?????? 00??? 00???", "010-1234-5678",
                    testerEmail, "admin", "password1!", "admin", "??????"), academyId);
            employeeService.createEmployee(new CreateEmployeeRequest("?????????", "00??? 00??? 00?????? 00??? 00???", "010-4321-8765",
                    "teacher@gmail.com", "teacher", "password1!", "USER", "??????"), academyId);
            employeeService.createEmployee(new CreateEmployeeRequest("?????????", "00??? 00??? 00?????? 00??? 00???", "010-1357-2468",
                    "staff@gmail.com", "staff", "password1!", "STAFF", "??????"), academyId);
            announcementService.createAnnouncement(academyId, "admin", new CreateAnnouncementRequest("????????? ????????? ??????", "<p>?????????????????? ???????????? ???????????????.</p><p><br></p><p>???????????????????????? ??? 3?????? ????????? ???????????????.</p><p><br></p><p>?????? ????????? ?????? ????????? ????????? ????????? ??? ?????????, ?????? ?????? ????????? ?????? ????????? ????????? ??? ????????????. ??????, ?????? ????????? ????????? ??? ????????????.</p><p><br></p><p>?????? ????????? ?????? ????????? ?????? ??????/ ????????? ?????? ????????? ????????? ?????? ????????? ????????? ??? ????????????.</p><p><br></p><p>?????? ????????? ?????? ???????????? ????????? ????????? ?????? ????????? ?????? ?? ?????? ?? ????????? ??? ??? ?????? ????????? ???????????????.</p><p><br></p><p>1. ?????? ?????? ?????????</p><p><br></p><p>????????? : ???????????? ??????</p><p>????????? : admin</p><p>???????????? : password1!</p><p><br></p><p>2. ?????? ?????? ?????????</p><p><br></p><p>????????? : ???????????? ??????</p><p>????????? : teacher</p><p>???????????? : password1!</p><p><br></p><p>3. ?????? ?????? ?????????</p><p><br></p><p>????????? : ???????????? ??????</p><p>????????? : staff</p><p>???????????? : password1!</p><p><br></p><p>??? ???????????? ????????????, ?????? ????????? ???????????? ???????????? ??????????????? ??? ????????????.</p>", AnnouncementType.ANNOUNCEMENT));
        }

        LoginEmployeeResponse loginEmployee = employeeService.loginEmployee(new LoginEmployeeRequest("admin", "password1!"), academyId);
        String token = loginEmployee.getJwt();
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.addCookie(httpServletResponse, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1??????
        log.info("???? ????????? ????????? ?????? {}", token);


        return "redirect:/academy/main";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request, academy);
        model.addAttribute("academy", academy);
        model.addAttribute("localDateTime", LocalDateTime.now());
        return academy;
    }

    private ReadEmployeeResponse setSessionEmployeeInfo(HttpServletRequest request, Model model, Authentication authentication, Long academyId) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //view ??? ?????? ??????, ?????? ?????? ????????? ??????
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionEmployeeNameAndRole(request, employee);
        model.addAttribute("employee", employee);
        return employee;
    }

}
