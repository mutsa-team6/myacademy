package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.enrollment.dto.*;
import com.project.myacademy.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.myacademy.global.util.AuthenticationUtil;
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

@Tag(name = "08. 수강신청", description = "수강 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/academies")
@Slf4j
public class EnrollmentRestController {

    private final EnrollmentService enrollmentService;

    // 수강 등록
    @Operation(summary = "수강 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다.")
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments")
    public ResponseEntity<Response<CreateEnrollmentResponse>> create(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @RequestBody CreateEnrollmentRequest request,
                                                                     Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateEnrollmentResponse createdEnrollment = enrollmentService.createEnrollment(academyId, studentId, lectureId, request, account);
        log.info("수강 등록 성공");
        return ResponseEntity.ok().body(Response.success(createdEnrollment));
    }

    // 수강 전체 리스트 조회
    @Operation(summary = "수강 전체 조회", description = "수강 리스트를 조회합니다.")
    @GetMapping("/{academyId}/enrollments")
    public ResponseEntity<Response<Page<ReadAllEnrollmentResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
             @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        Page<ReadAllEnrollmentResponse> enrollments = enrollmentService.readAllEnrollments(academyId, account, pageable);
        log.info("수강 리스트 조회");
        return ResponseEntity.ok().body(Response.success(enrollments));
    }

    // 수강 수정
    @Operation(summary = "수강 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}")
    public ResponseEntity<Response<UpdateEnrollmentResponse>> update(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @PathVariable("enrollmentId") Long enrollmentId,
                                                                     @RequestBody UpdateEnrollmentRequest request,
                                                                     Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateEnrollmentResponse updatedEnrollment = enrollmentService.updateEnrollment(academyId, studentId, lectureId, enrollmentId, request, account);
        log.info("수강 이력 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedEnrollment));
    }

    // 수강 삭제
    @Operation(summary = "수강 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n 수강이력이 삭제되고, 다음 대기번호를 확인한 후, 수강 등록이 되면 기존 대기번호가 삭제됩니다.")
    @PostMapping("/{academyId}/students/{studentId}/lectures/{lectureId}/enrollments/{enrollmentId}")
    public ResponseEntity<Response<DeleteEnrollmentResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                     @PathVariable("studentId") Long studentId,
                                                                     @PathVariable("lectureId") Long lectureId,
                                                                     @PathVariable("enrollmentId") Long enrollmentId,
                                                                     @RequestBody CreateEnrollmentRequest request,
                                                                     Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteEnrollmentResponse deletedEnrollment = enrollmentService.deleteEnrollment(academyId, studentId, lectureId, enrollmentId, request, account);
        log.info("수강 이력 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedEnrollment));
    }
}