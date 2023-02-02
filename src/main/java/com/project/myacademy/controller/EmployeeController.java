package com.project.myacademy.controller;

import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final LectureService lectureService;

    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;

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
    public String mypage(Model model, Authentication authentication, Pageable pageable) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        log.info("🔎 마이페이지 조회한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);

        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        model.addAttribute("employee", employee);

        Page<ReadAllLectureResponse> lectures = null;
        if (!employee.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
            lectures = lectureService.readAllLecturesByTeacherId(academyId, requestAccount, employee.getId(), pageable);
        }
        model.addAttribute("academyId", academyId);
        model.addAttribute("employeeId", employee.getId());
        model.addAttribute("lectures", lectures);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        return "employee/mypage";
    }


    @GetMapping("/oauthFail")
    public void oauthFail(HttpServletResponse response) throws IOException {
        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('가입된 회원이 아닙니다. 가입 후 소셜 로그인 서비스를 이용해주세요.');  location.href='/join'</script>");
        writer.flush();
    }

    @GetMapping("/logoutEmployee")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.addCookie(response, "deleted");
        cookieGenerator.setCookieMaxAge(0);

        HttpSession session = request.getSession();
        session.removeAttribute("name");
        return "redirect:/";
    }

    @GetMapping("/oauth2/redirect")
    public String login(@RequestParam String token, HttpServletResponse response) {
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.addCookie(response, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1시간
        log.info("🍪 쿠키에 저장한 토큰 {}", token);
        return "redirect:/academy/main";
    }
}
