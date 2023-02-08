package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.discount.DiscountService;
import com.project.myacademy.domain.discount.dto.GetDiscountResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
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

@Controller
@Slf4j
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    private final AcademyService academyService;
    private final EmployeeService employeeService;

    @GetMapping("academy/discount")
    public String discount(HttpServletRequest request,Authentication authentication, Model model, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        //할인 정책 목록을 보여주기 위해, 해당 학원이 갖고 있는 학원 정책을 모두 가져온다.
        Page<GetDiscountResponse> discounts = discountService.getAllDiscounts(academyId, requestAccount, pageable);
        model.addAttribute("discounts", discounts);


        return "pages/discount";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
        model.addAttribute("academy", academy);
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
