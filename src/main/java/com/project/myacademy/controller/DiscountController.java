package com.project.myacademy.controller;

import com.project.myacademy.domain.discount.DiscountService;
import com.project.myacademy.domain.discount.dto.GetDiscountResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping("academy/discount")
    public String discount(Authentication authentication, Model model, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        Page<GetDiscountResponse> discounts = discountService.getAllDiscounts(academyId, requestAccount, pageable);
        model.addAttribute("academyId", academyId);
        model.addAttribute("discounts", discounts);


        return "pages/discount";
    }
}
