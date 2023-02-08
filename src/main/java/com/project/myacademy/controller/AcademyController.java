package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyForUIResponse;
import com.project.myacademy.domain.academy.dto.FindAcademyRequest;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @GetMapping("/academies")
    public String academy() {
        return "academy/academies";
    }

    @ResponseBody
    @PostMapping("/academies/check")
    public FindAcademyForUIResponse checkAcademyExist(@RequestBody FindAcademyRequest request) {

        FindAcademyForUIResponse response = new FindAcademyForUIResponse(false, 0L);

        // 해당 학원 이름과 일치하는 학원 존재시 true 반환
        boolean isExist = academyService.checkExistByAcademyName(request.getName());

        // 학원 이름과 일치하는 데이터가 있을 시, 쿼리문 한번 더 실행
        if (isExist) {
            FindAcademyResponse foundAcademy = academyService.findAcademy(request);
            response = new FindAcademyForUIResponse(isExist, foundAcademy.getAcademyId());
        }

        // 학원 존재 유무 T/F 와 회원 가입 진행을 위해 academy Id를 반환한다.

        return response;
    }
}
