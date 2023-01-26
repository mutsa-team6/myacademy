package com.project.myacademy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
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
public class EmployeeController {

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
        cookieGenerator.setCookieSecure(true);
        cookieGenerator.addCookie(response, token);
        cookieGenerator.setCookieMaxAge(60 * 60);//1시간
        log.info("🍪 쿠키에 저장한 토큰 {}", token);
        return "redirect:/main";
    }
}
