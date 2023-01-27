package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.lecture.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class LectureRestController {

    private final LectureService lectureService;

    // 강좌 전체 조회
   @GetMapping("/{academyId}/lectures")
    public ResponseEntity<Response<Page<ReadAllLectureResponse>>> readAll(@PathVariable("academyId") Long academyId, Authentication authentication,
              @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
       Page<ReadAllLectureResponse> lectures = lectureService.readAllLectures(academyId, authentication.getName(), pageable);
        log.info("강좌 리스트 조회 성공");
        return ResponseEntity.ok().body(Response.success(lectures));
    }

    // 강좌 등록
    @PostMapping("/{academyId}/teachers/{teacherId}/lectures")
    public ResponseEntity<Response<CreateLectureResponse>> create(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @RequestBody CreateLectureRequest request,
                                                                  Authentication authentication) {
        CreateLectureResponse createdLecture = lectureService.createLecture(academyId, teacherId, request, authentication.getName());
        log.info("강좌 정보 생성 성공");
        return ResponseEntity.ok().body(Response.success(createdLecture));
    }

    // 강좌 수정
    @PutMapping("/{academyId}/teachers/{teacherId}/lectures/{lectureId}")
    public ResponseEntity<Response<UpdateLectureResponse>> update(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @PathVariable("lectureId") Long lectureId,
                                                                  @RequestBody UpdateLectureRequest request,
                                                                  Authentication authentication) {
        UpdateLectureResponse updatedLecture = lectureService.updateLecture(academyId, teacherId, lectureId, request, authentication.getName());
        log.info("강좌 정보 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedLecture));
    }

    // 강좌 삭제
    @DeleteMapping("/{academyId}/teachers/{teacherId}/lectures/{lectureId}")
    public ResponseEntity<Response<DeleteLectureResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @PathVariable("lectureId") Long lectureId,
                                                                  Authentication authentication) {
        DeleteLectureResponse deletedLecture = lectureService.deleteLecture(academyId, teacherId, lectureId, authentication.getName());
        log.info("강좌 정보 삭제 성공");
        return ResponseEntity.ok(Response.success(deletedLecture));
    }
}