package com.project.myacademy.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
public class IndexController {


    @GetMapping("/main")
    public String index(HttpServletRequest request, Model model){

        //회원 이름 표시
        HttpSession session = request.getSession(true);

        if (session.getAttribute("name") != null) {
            String loginUserName = (String)session.getAttribute("name");
            log.info("세션에 저장된 실명 : [{}]",loginUserName);
            model.addAttribute("name", loginUserName);
        }
        return "main";
    }
    @GetMapping("/join")
    public String join(){
        return "join";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/oauthFail")
    public String oauthFail(){
        return "oauthFail";
    }

    @GetMapping("/logoutEmployee")
    public String logout(HttpServletRequest request,HttpServletResponse response){
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.addCookie(response,"deleted");
        cookieGenerator.setCookieMaxAge(0);

        HttpSession session = request.getSession();
        session.removeAttribute("name");
        return "redirect:/";
    }

    @GetMapping("/oauth2/redirect")
    public String login(@RequestParam String token, HttpServletResponse response){
        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.setCookieHttpOnly(true);
        cookieGenerator.setCookieSecure(true);
        cookieGenerator.addCookie(response,token);
        cookieGenerator.setCookieMaxAge(60*60);//1시간
        log.info("🍪 쿠키에 저장한 토큰 {}",token);
        return "redirect:/main";
    }


}
