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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class LectureRestController {

    /**
     * 현재, 각 메서드에 Authentication authentication 빠진 상태
     * 구현되면 각 서비스 메서드에 authentication.getXXX() 식으로 넘길 예정
     * Academy, Employee 모두 넘길 예정
     */

    private final LectureService lectureService;

    @GetMapping("/{academyId}/lectures")
    public ResponseEntity<Response<Page<ReadAllLectureResponse>>> readAll(@PathVariable("academyId") Long academyId,
            @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReadAllLectureResponse> lectures = lectureService.readAllLectures(academyId, pageable);
        log.info("강좌 리스트 조회 성공");
        return ResponseEntity.ok().body(Response.success(lectures));
    }

    @PostMapping("/{academyId}/teachers/{teacherId}/lectures")
    public ResponseEntity<Response<CreateLectureResponse>> create(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @RequestBody CreateLectureRequest request) {
        CreateLectureResponse createdLecture = lectureService.createLecture(academyId, teacherId, request);
        log.info("강좌 정보 생성 성공");
        return ResponseEntity.ok().body(Response.success(createdLecture));
    }

    @PutMapping("/{academyId}/lectures/{lectureId}")
    public ResponseEntity<Response<UpdateLectureResponse>> update(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("lectureId") Long lectureId,
                                                                  @RequestBody UpdateLectureRequest request) {
        UpdateLectureResponse updatedLecture = lectureService.updateLecture(academyId, lectureId, request);
        log.info("강좌 정보 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedLecture));
    }

    @DeleteMapping("/{academyId}/lectures/{lectureId}")
    public ResponseEntity<Response<DeleteLectureResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("lectureId") Long lectureId) {
        DeleteLectureResponse deletedLecture = lectureService.deleteLecture(academyId, lectureId);
        log.info("강좌 정보 삭제 성공");
        return ResponseEntity.ok(Response.success(deletedLecture));
    }
}