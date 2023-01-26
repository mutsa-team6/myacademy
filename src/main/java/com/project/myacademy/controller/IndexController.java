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



}
