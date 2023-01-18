package com.project.myacademy.domain.studentlecture;

import com.project.myacademy.domain.studentlecture.dto.CreateStudentLectureRequest;
import com.project.myacademy.domain.studentlecture.dto.CreateStudentLectureResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
@Slf4j
public class StudentLectureRestController {

    private final StudentLectureService studentLectureService;

    @PostMapping("/{studentId}/lectures/{lectureId}/studentlectures")
    public ResponseEntity<Response<CreateStudentLectureResponse>> create(@PathVariable("studentId") Long studentId,
                                                                         @PathVariable("lectureId") Long lectureId,
                                                                         @RequestBody CreateStudentLectureRequest request) {

        CreateStudentLectureResponse createdStudentLecture = studentLectureService.createStudentLecture(studentId, lectureId, request);
        log.info("학생-수강 등록 성공");
        return ResponseEntity.ok().body(Response.success((createdStudentLecture)));
    }
}
