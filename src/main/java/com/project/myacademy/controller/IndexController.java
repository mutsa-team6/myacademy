package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.announcement.AnnouncementService;
import com.project.myacademy.domain.announcement.dto.ReadAnnouncementResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
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

import javax.servlet.http.HttpServletRequest;
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

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
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
        model.addAttribute("lectures",lectures);

        String imageUrl = academyProfileS3UploadService.getStoredUrl(academyId);
        model.addAttribute("imageUrl", imageUrl);


        // 강의 종료일이 지나지 않은 강의 목록만 가져온다.
        // 해당 강의의 대기 인원이 나오고, 어떤 학생이 대기중인지, 어떤 학생이 등록했는지 보여주기 위함
        for (ReadAllLectureResponse lecture : lectures) {
            lecture.setRegisteredStudent(enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academyId, requestAccount, lecture.getLectureId()));
        }
        model.addAttribute("lectures", lectures);


        return "pages/main";
    }
    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
        model.addAttribute("academy", academy);
        model.addAttribute("localDateTime",LocalDateTime.now());
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
