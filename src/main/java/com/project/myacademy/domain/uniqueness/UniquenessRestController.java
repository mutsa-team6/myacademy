package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessRequest;
import com.project.myacademy.domain.uniqueness.dto.CreateUniquenessResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class UniquenessRestController {

    private final UniquenessService uniquenessService;

    /**
     *
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param request 특이사항의 요청시 받는 request Dto
     */
    @PostMapping("students/{studentId}/uniquness")
    public ResponseEntity<Response<CreateUniquenessResponse>> create(@PathVariable Long studentId, CreateUniquenessRequest request) {
        CreateUniquenessResponse response = uniquenessService.createUniqueness(studentId,request);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
