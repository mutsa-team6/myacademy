package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final EmployeeService employeeService;
    private final AcademyService academyService;

    @GetMapping("/academy/main")
    public String main(HttpServletRequest request, Model model, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("⭐ 메인 요청한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);


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
            if (found.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
                session.setAttribute("role", "강사");
            } else if (found.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
                session.setAttribute("role", "직원");
            } else {
                session.setAttribute("role", "원장");

            }
            model.addAttribute("name", foundName);
        }
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);

        return "pages/main";
    }


}
