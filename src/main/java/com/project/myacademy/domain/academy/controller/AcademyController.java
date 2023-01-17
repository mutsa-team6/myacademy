package com.project.myacademy.domain.academy.controller;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.domain.academy.service.AcademyService;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class AcademyController {

    private final AcademyService academyService;

    /**
     * 학원 등록
     *
     * @param request
     * @return ResponseEntity
     */
    @PostMapping("/join")
    public ResponseEntity create(@RequestBody CreateAcademyRequest request) {
        AcademyDto savedAcademyDto = academyService.createAcademy(request);

        return ResponseEntity.ok(Response.success(new CreateAcademyResponse(
                savedAcademyDto.getId(),
                savedAcademyDto.getName(),
                savedAcademyDto.getOwner(),
                "학원 등록이 정상적으로 완료되었습니다.")));
    }
}
