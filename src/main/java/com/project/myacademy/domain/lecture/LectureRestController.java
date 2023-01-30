package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.lecture.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "07. 강좌", description = "강좌 등록,수정,조회")
@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class LectureRestController {

    private final LectureService lectureService;

    // 강좌 전체 조회
    @Operation(summary = "강좌 전체 조회", description = "모든 강좌를 조회합니다.")
    @GetMapping("/{academyId}/lectures")
    public ResponseEntity<Response<Page<ReadAllLectureResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
              @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
       String account = AuthenticationUtil.getAccountFromAuth(authentication);
       Page<ReadAllLectureResponse> lectures = lectureService.readAllLectures(academyId, account, pageable);
        log.info("강좌 리스트 조회 성공");
        return ResponseEntity.ok().body(Response.success(lectures));
    }

    // 강좌 등록
    @Operation(summary = "강좌 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다.")
    @PostMapping("/{academyId}/employees/{employeeId}/lectures")
    public ResponseEntity<Response<CreateLectureResponse>> create(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("employeeId") Long employeeId,
                                                                  @RequestBody CreateLectureRequest request,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateLectureResponse createdLecture = lectureService.createLecture(academyId, employeeId, request, account);
        log.info("강좌 정보 생성 성공");
        return ResponseEntity.ok().body(Response.success(createdLecture));
    }

    // 강좌 수정
    @Operation(summary = "강좌 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/lectures/{lectureId}")
    public ResponseEntity<Response<UpdateLectureResponse>> update(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("lectureId") Long lectureId,
                                                                  @RequestBody UpdateLectureRequest request,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateLectureResponse updatedLecture = lectureService.updateLecture(academyId, lectureId, request, account);
        log.info("강좌 정보 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedLecture));
    }

    // 강좌 삭제
    @Operation(summary = "강좌 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n soft-delete 됩니다.")
    @DeleteMapping("/{academyId}/lectures/{lectureId}")
    public ResponseEntity<Response<DeleteLectureResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("lectureId") Long lectureId,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteLectureResponse deletedLecture = lectureService.deleteLecture(academyId, lectureId, account);
        log.info("강좌 정보 삭제 성공");
        return ResponseEntity.ok(Response.success(deletedLecture));
    }
}