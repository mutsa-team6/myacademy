package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.uniqueness.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/students")
public class UniquenessRestController {

    private final UniquenessService uniquenessService;

    /**
     * 학생 특이사항 작성
     */
    @PostMapping("/{studentId}/uniqueness")
    public ResponseEntity<Response<CreateUniquenessResponse>> create(@PathVariable Long studentId, CreateUniquenessRequest request, Authentication authentication) {
        String account = authentication.getName();
        CreateUniquenessResponse response = uniquenessService.createUniqueness(studentId, request, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학생 특이사항 목록 조회
     */
    @GetMapping("/{studentId}/uniqueness")
    public ResponseEntity<Response<Page<ReadAllUniquenessResponse>>> readAll(@PathVariable Long studentId, Authentication authentication) {
        String account = authentication.getName();
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllUniquenessResponse> responses = uniquenessService.readAllUniqueness(studentId, pageable, account);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 특정 특이사항 수정
     */
    @PutMapping("/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<UpdateUniquenessResponse>> update(@PathVariable Long studentId, @PathVariable Long uniquenessId, UpdateUniquenessRequest request, Authentication authentication) {
        String account = authentication.getName();
        UpdateUniquenessResponse response = uniquenessService.updateUniqueness(studentId, uniquenessId, request, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 특이사항 수정
     */
    @DeleteMapping("/{studentId}/uniqueness/{uniquenessId}")
    public ResponseEntity<Response<DeleteUniquenessResponse>> delete(@PathVariable Long studentId, @PathVariable Long uniquenessId, Authentication authentication) {
        String account = authentication.getName();
        DeleteUniquenessResponse response = uniquenessService.deleteUniqueness(studentId, uniquenessId, account);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
