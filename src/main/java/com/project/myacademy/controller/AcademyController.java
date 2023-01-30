package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyForUIResponse;
import com.project.myacademy.domain.academy.dto.FindAcademyRequest;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @GetMapping("/academies")
    public String academy(Model model, Pageable pageable) {

        Page<ReadAcademyResponse> academies = academyService.readAllAcademies(pageable);

        model.addAttribute("academies", academies);

        return "academy/academies";
    }

    @ResponseBody
    @PostMapping("/academies/check")
    public FindAcademyForUIResponse checkAcademyExist(@RequestBody FindAcademyRequest request) {
        log.info("🔎 찾으려는 학원 이름 [{}]",request.getName());
        boolean isExist = academyService.checkExistByAcademyName(request.getName());
        FindAcademyResponse found = academyService.findAcademy(request);
        FindAcademyForUIResponse response = new FindAcademyForUIResponse(isExist, found.getAcademyId());
        return response;
    }
}
