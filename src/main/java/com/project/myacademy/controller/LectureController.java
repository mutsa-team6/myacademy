package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LectureController {
    private final EmployeeService employeeService;

    @GetMapping("/academy/lecture")
    public String main(HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("⭐ 강좌 등록 요청한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);


        //회원 이름 표시
        HttpSession session = request.getSession(true);

        if (session.getAttribute("name") != null) {
            String loginUserName = (String) session.getAttribute("name");
            log.info("세션에 저장된 실명 : [{}]", loginUserName);
            model.addAttribute("name", loginUserName);
        } else {
            ReadEmployeeResponse found = employeeService.readEmployee(academyId, requestAccount);
            Academy foundAcademy = found.getAcademy();
            String foundName = found.getName();
            session.setAttribute("name", foundName);
            model.addAttribute("name", foundName);
        }

        Page<ReadEmployeeResponse> teachers = employeeService.findAllTeachers(requestAccount, academyId, pageable);
        model.addAttribute("teachers", teachers);

        return "pages/lecture";
    }


}
