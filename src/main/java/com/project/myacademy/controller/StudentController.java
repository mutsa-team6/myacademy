package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import com.project.myacademy.domain.parent.ParentService;
import com.project.myacademy.domain.parent.dto.FindParentResponse;
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

@Controller
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final AcademyService academyService;
    private final ParentService parentService;

    @GetMapping("/academy/student")
    public String student(Model model, Pageable pageable) {

        Page<ReadAcademyResponse> academies = academyService.readAllAcademies(pageable);

        model.addAttribute("academies", academies);

        return "pages/student";
    }

    @GetMapping("/academy/student/register")
    public String studentRegister(@RequestParam(required = false) String parentPhoneNum, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        Page<ReadAcademyResponse> academies = academyService.readAllAcademies(pageable);

        if (parentPhoneNum != null) {
            FindParentResponse parent = parentService.findParent(parentPhoneNum, academyId);
            model.addAttribute("parent", parent);
        }

        model.addAttribute("academies", academies);
        model.addAttribute("academyId", academyId);

        return "student/register";
    }
}
