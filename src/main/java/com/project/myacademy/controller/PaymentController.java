package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;

import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final EmployeeService employeeService;
    private final EnrollmentService enrollmentService;
    @Value("${payment.toss.testClientApiKey}")
    private String key;

    @GetMapping("/academy/pay")
    public String main(@RequestParam(required = false) String studentName,HttpServletRequest request, Model model, Authentication authentication){

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("⭐ 메인 요청한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]",academyId,requestAccount);

        if (studentName != null) {

            List<FindEnrollmentResponse> enrollments = enrollmentService.findEnrollmentForPay(academyId, studentName);
            log.info("⭐ 검색 학생 이름 [{}] || 강좌 수 [{}] ",studentName,enrollments.size());
            model.addAttribute("enrollments", enrollments);
        }
        log.info("🔑 key = {}",key);
        model.addAttribute("tossKey", key);

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
            session.setAttribute("name",foundName);
            model.addAttribute("name", foundName);
        }
        return "pages/payment";
    }



}
