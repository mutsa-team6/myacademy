package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadAllEmployeeResponse;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindStudentInfoFromEnrollmentByLectureResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenService;
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
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LectureService lectureService;
    private final AcademyService academyService;
    private final EnrollmentService enrollmentService;
    private final RefreshTokenService refreshTokenService;

    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;

    private static final String PREVIOUS = "previous";
    private static final String NEXT = "next";
    private static final String JWT_TOKEN_NAME = "token";

    @GetMapping("/join")
    public String join() {
        return "employee/join";
    }

    @GetMapping("/login")
    public String login() {
        return "employee/login";
    }

    @GetMapping("/find/account")
    public String findAccount() {

        return "employee/find";
    }

    @GetMapping("/find/password")
    public String findPassword() {

        return "employee/findPassword";
    }

    @GetMapping("/academy/mypage")
    public String mypage(HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        //?????? ????????? ?????? ????????????
        String storedUrl = employeeProfileS3UploadService.getStoredUrl(requestEmployee.getId());
        model.addAttribute("imageUrl", storedUrl);

        Page<ReadAllLectureResponse> lectures = null;

        // ?????? ?????????????????? ????????? ????????? ?????? ????????? ??????, ????????? ?????? ????????? ????????? ??????, ????????? ?????? ????????? ????????????.
        if (!requestEmployee.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
            lectures = lectureService.readAllLecturesByTeacherId(academyId,requestAccount, requestEmployee.getId(), pageable);

            //????????? ????????? ???????????? (????????? ??????????????? ??????) ???????????? ????????? ????????? ?????? ????????????.
            lectures.stream().forEach(readAllLectureResponse ->
                    readAllLectureResponse
                            .setCompletePaymentNumber(enrollmentService
                                    .findStudentInfoFromEnrollmentByLecture(academyId, requestAccount, readAllLectureResponse.getLectureId(), pageable)
                                    .getTotalElements()));
        }
        model.addAttribute("lectures", lectures);


        model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
        model.addAttribute(NEXT, pageable.next().getPageNumber());

        return "employee/mypage";
    }



    /**
     * ????????? ??????
     */
    @GetMapping("/academy/employees")
    public String manageEmployee(HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        // ?????? ????????? ???????????? ?????? ?????? ?????? ?????? (?????? ?????? ???????????? ??????, ?????? ???????????????)
        Page<ReadAllEmployeeResponse> foundEmployees = employeeService.readAllEmployees(requestAccount, academyId, pageable);

        // ????????? ????????? ????????? ???????????? ??????, ??????
        for (ReadAllEmployeeResponse employee : foundEmployees) {
            employee.setImageUrl(employeeProfileS3UploadService.getStoredUrl(employee.getId()));
        }
        model.addAttribute("employees", foundEmployees);


        model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
        model.addAttribute(NEXT, pageable.next().getPageNumber());

        return "employee/employees";
    }

    @GetMapping("/academy/mypage/attendance")
    public String attendance(@RequestParam Long lectureId, HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        //?????? ????????? lectureId??? ??????????????? ?????????, ?????? ????????? ????????? ????????? ????????? ?????? ????????? ?????????
        Page<FindStudentInfoFromEnrollmentByLectureResponse> studentsInfo
                = enrollmentService.findStudentInfoFromEnrollmentByLecture(academyId, requestAccount, lectureId, pageable);


        model.addAttribute("studentsInfo", studentsInfo);

        model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
        model.addAttribute(NEXT, pageable.next().getPageNumber());

        return "employee/attendance";
    }

    @GetMapping("/oauthFail")
    public void oauthFail(HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('????????? ????????? ????????????. ?????? ??? ?????? ????????? ???????????? ??????????????????.');  location.href='/join'</script>");
        writer.flush();
    }

    @GetMapping("/logoutEmployee")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] list = request.getCookies();

        String token = null;
        for (Cookie cookie : list) {
            if (cookie.getName().equals(JWT_TOKEN_NAME)) {
                token = cookie.getValue();

            }
        }
        if (token != null) {
            refreshTokenService.removeRefreshToken(token);
        }

        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName(JWT_TOKEN_NAME);
        cookieGenerator.addCookie(response, "deleted");
        cookieGenerator.setCookieMaxAge(0);

        HttpSession session = request.getSession();
        session.removeAttribute("name");
        return "redirect:/";
    }

    @GetMapping("/oauth2/redirect")
    public String login(@RequestParam String token, @RequestParam String refreshToken, @RequestParam Long employeeId, HttpServletResponse response) {

        refreshTokenService.saveTokenInfo(employeeId, refreshToken, token);

        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName(JWT_TOKEN_NAME);
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.addCookie(response, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1??????
        log.info("???? ????????? ????????? ?????? {}", token);
        return "redirect:/academy/main";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
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
