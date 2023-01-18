package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.uniqueness.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class UniquenessRestController {

    private final UniquenessService uniquenessService;

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param request   특이사항의 요청시 받는 request Dto
     */
    @PostMapping("students/{studentId}/uniqueness")
    public ResponseEntity<Response<CreateUniquenessResponse>> create(@PathVariable Long studentId, CreateUniquenessRequest request) {
        CreateUniquenessResponse response = uniquenessService.createUniqueness(studentId, request);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     */
    @GetMapping("students/{studentId}/uniqueness")
    public ResponseEntity<Response<Page<ReadAllUniquenessResponse>>> readAll(@PathVariable Long studentId) {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllUniquenessResponse> responses = uniquenessService.readAllUniqueness(studentId, pageable);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     *
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 수정하려고하는 특이사항 Id
     * @param request 수정내용이 담긴 dto
     */
    @PutMapping("students/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<UpdateUniquenessResponse>> update(@PathVariable Long studentId, @PathVariable Long uniquenessId, UpdateUniquenessRequest request) {
     UpdateUniquenessResponse response = uniquenessService.updateUniqueness(studentId, uniquenessId, request);
     return ResponseEntity.ok().body(Response.success(response));
    }
}
