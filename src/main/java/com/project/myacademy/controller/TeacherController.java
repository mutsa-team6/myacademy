package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TeacherController {

    private final AcademyService academyService;

    @GetMapping("/academy/teacher")
    public String academy(Model model, Pageable pageable) {

        Page<ReadAcademyResponse> academies = academyService.readAllAcademies(pageable);

        model.addAttribute("academies", academies);

        return "pages/teacher";
    }
}
