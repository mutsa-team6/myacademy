package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.enrollment.dto.*;
import com.project.myacademy.global.Response;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "수강신청")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academies")
@Slf4j
public class EnrollmentRestController {

    private final EnrollmentService enrollmentService;

    // 수강 등록
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments")
    public ResponseEntity<Response<CreateEnrollmentResponse>> create(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @RequestBody CreateEnrollmentRequest request,
                                                                     Authentication authentication) {
        CreateEnrollmentResponse createdEnrollment = enrollmentService.createEnrollment(academyId, studentId, lectureId, request, authentication.getName());
        log.info("수강 등록 성공");
        return ResponseEntity.ok().body(Response.success(createdEnrollment));
    }

    // 수강 전체 리스트 조회
    @GetMapping("/{academyId}/enrollments")
    public ResponseEntity<Response<Page<ReadAllEnrollmentResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
             @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReadAllEnrollmentResponse> enrollments = enrollmentService.readAllEnrollments(academyId, authentication.getName(), pageable);
        log.info("수강 리스트 조회");
        return ResponseEntity.ok().body(Response.success(enrollments));
    }

    // 수강 수정
    @PutMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}")
    public ResponseEntity<Response<UpdateEnrollmentResponse>> update(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @PathVariable("enrollmentId") Long enrollmentId,
                                                                     @RequestBody UpdateEnrollmentRequest request,
                                                                     Authentication authentication) {
        UpdateEnrollmentResponse updatedEnrollment = enrollmentService.updateEnrollment(academyId, studentId, lectureId, enrollmentId, request, authentication.getName());
        log.info("수강 이력 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedEnrollment));
    }

    // 수강 삭제
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}")
    public ResponseEntity<Response<DeleteEnrollmentResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @PathVariable("enrollmentId") Long enrollmentId,
                                                                     @RequestBody CreateEnrollmentRequest request,
                                                                     Authentication authentication) {
        DeleteEnrollmentResponse deletedEnrollment = enrollmentService.deleteEnrollment(academyId, studentId, lectureId, enrollmentId, request, authentication.getName());
        log.info("수강 이력 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedEnrollment));
    }
}