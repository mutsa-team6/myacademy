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
        model.addAttribute("lectures", lectures);

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

    @GetMapping("/about")
    public String about() {
        return "about/about";
    }

    @GetMapping("/tester")
    public String testerLogin(HttpServletResponse httpServletResponse) {
        Long academyId;
        String testerEmail = "tester@gmail.com";
        // 학원 생성 (테스터용 학원 데이터가 없는 경우 생성)
        if (!academyService.checkExistByAcademyName("테스트용 학원")) {
            CreateAcademyResponse academy = academyService.createAcademy(new CreateAcademyRequest("테스트용 학원", "OO 학원 주소", "010-0000-0000", "테스터", "12-345-67890"));
            academyId = academy.getAcademyId();
        } else {
            FindAcademyResponse academy = academyService.findAcademy(new FindAcademyRequest("테스트용 학원"));
            academyId = academy.getAcademyId();
        }

        // 아직 테스터 아이디가 생성되어있지 않는 경우 원장, 강사 직원 아이디 생성
        if (!employeeService.checkExistByEmployeeEmail(testerEmail)) {
            CreateEmployeeResponse employee = employeeService.createEmployee(new CreateEmployeeRequest("테스터", "00시 00로 00번지 00동 00호", "010-1234-5678",
                    testerEmail, "admin", "password1!", "admin", "원장"), academyId);
            employeeService.createEmployee(new CreateEmployeeRequest("김철수", "00시 00로 00번지 00동 00호", "010-4321-8765",
                    "teacher@gmail.com", "teacher", "password1!", "USER", "수학"), academyId);
            employeeService.createEmployee(new CreateEmployeeRequest("김영희", "00시 00로 00번지 00동 00호", "010-1357-2468",
                    "staff@gmail.com", "staff", "password1!", "STAFF", "직원"), academyId);
            announcementService.createAnnouncement(academyId, "admin", new CreateAnnouncementRequest("테스터 로그인 안내", "<p>마이아카데미 테스터님 환영합니다.</p><p><br></p><p>마이아카데미에는 총 3가지 권한이 존재합니다.</p><p><br></p><p>원장 계정은 학원 프로필 사진을 추가할 수 있으며, 직원 관리 탭에서 직원 정보를 조회할 수 있습니다. 또한, 모든 기능을 사용할 수 있습니다.</p><p><br></p><p>직원 계정은 학원 프로필 사진 추가/ 임직원 조회 기능을 제외한 모든 기능을 사용할 수 있습니다.</p><p><br></p><p>강사 계정은 학생 특이사항 등록을 제외한 모든 데이터 입력 · 수정 · 삭제를 할 수 없고 조회만 가능합니다.</p><p><br></p><p>1. 원장 계정 로그인</p><p><br></p><p>학원명 : 테스트용 학원</p><p>계정명 : admin</p><p>비밀번호 : password1!</p><p><br></p><p>2. 강사 계정 로그인</p><p><br></p><p>학원명 : 테스트용 학원</p><p>계정명 : teacher</p><p>비밀번호 : password1!</p><p><br></p><p>3. 직원 계정 로그인</p><p><br></p><p>학원명 : 테스트용 학원</p><p>계정명 : staff</p><p>비밀번호 : password1!</p><p><br></p><p>위 정보대로 입력하면, 해당 권한에 해당하는 아이디로 로그인하실 수 있습니다.</p>", AnnouncementType.ANNOUNCEMENT));
        }

        LoginEmployeeResponse loginEmployee = employeeService.loginEmployee(new LoginEmployeeRequest("admin", "password1!"), academyId);
        String token = loginEmployee.getJwt();
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.addCookie(httpServletResponse, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1시간
        log.info("🍪 쿠키에 저장한 토큰 {}", token);


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

        //view 에 회원 계정, 회원 직책 세션에 저장
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionEmployeeNameAndRole(request, employee);
        model.addAttribute("employee", employee);
        return employee;
    }

}
