package com.project.myacademy.domain.studentlecture;

import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.studentlecture.dto.*;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class StudentLectureRestController {

    private final StudentLectureService studentLectureService;

    // 학생-수강 등록
    @PostMapping("/students/{studentId}/lectures/{lectureId}/payments/{paymentId}/studentlectures")
    public ResponseEntity<Response<CreateStudentLectureResponse>> create(@PathVariable("studentId") Long studentId,
                                                                         @PathVariable("lectureId") Long lectureId,
                                                                         @PathVariable("paymentId") Long paymentId,
                                                                         @RequestBody CreateStudentLectureRequest request,
                                                                         Authentication authentication) {
        CreateStudentLectureResponse createdStudentLecture = studentLectureService.createStudentLecture(studentId, lectureId, paymentId, request,authentication.getName());
        log.info("학생-수강 등록 성공");
        return ResponseEntity.ok().body(Response.success(createdStudentLecture));
    }

    // 학생-수강 전체 리스트 조회
//    @GetMapping("/studentlectures")
//    public ResponseEntity<Response<Page<ReadAllStudentLectureResponse>>> readAll(Authentication authentication,
//                @PageableDefault(size = 20, sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
//        Page<ReadAllStudentLectureResponse> studentLectures = studentLectureService.readAllStudentLectures(authentication.getName(), pageable);
//        log.info("학생-수강 리스트 조회");
//        return ResponseEntity.ok().body(Response.success(studentLectures));
//    }

    // 학생-수강 수정
    @PutMapping("/students/{studentId}/lectures/{lectureId}/payments/{paymentId}/studentlectures/{studentlectureId}")
    public ResponseEntity<Response<UpdateStudentLectureResponse>> update(@PathVariable("studentId") Long studentId,
                                                                         @PathVariable("lectureId") Long lectureId,
                                                                         @PathVariable("paymentId") Long paymentId,
                                                                         @PathVariable("studentlectureId") Long studentlectureId,
                                                                         @RequestBody UpdateStudentLectureRequest request,
                                                                         Authentication authentication) {
        UpdateStudentLectureResponse updatedStudentLecture = studentLectureService.updateStudentLecture(studentId, lectureId, paymentId, studentlectureId, request, authentication.getName());
        log.info("학생-수강 이력 수정 성공");
        return ResponseEntity.ok().body(Response.success(updatedStudentLecture));
    }

    // 학생-수강 삭제
    @DeleteMapping("/students/{studentId}/lectures/{lectureId}/payments/{paymentId}/studentlectures/{studentlectureId}")
    public ResponseEntity<Response<DeleteStudentLectureResponse>> delete(@PathVariable("studentId") Long studentId,
                                                                         @PathVariable("lectureId") Long lectureId,
                                                                         @PathVariable("paymentId") Long paymentId,
                                                                         @PathVariable("studentlectureId") Long studentlectureId,
                                                                         Authentication authentication) {
        DeleteStudentLectureResponse deletedStudentLecture = studentLectureService.deleteStudentLecture(studentId, lectureId, paymentId, studentlectureId, authentication.getName());
        log.info("학생-수강 이력 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedStudentLecture));
    }
}
