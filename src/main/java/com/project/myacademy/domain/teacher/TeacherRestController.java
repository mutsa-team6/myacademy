package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.teacher.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherRestController {

    private final TeacherService teacherService;

    // 강사 정보 등록
    @PostMapping("")
    public ResponseEntity<Response<CreateTeacherResponse>> create(@RequestBody CreateTeacherRequest request,
                                                                  Authentication authentication) {
        CreateTeacherResponse createdTeacher = teacherService.createTeacher(request, authentication.getName());
        log.info("강좌의 강사 배정 성공");
        return ResponseEntity.ok().body(Response.success(createdTeacher));
    }

    // 강사 정보 수정
    @PutMapping("/{teacherId}")
    public ResponseEntity<Response<UpdateTeacherResponse>> update(@PathVariable("teacherId") Long teacherId,
                                                                  @RequestBody UpdateTeacherRequest request,
                                                                  Authentication authentication) {

        UpdateTeacherResponse updatedTeacher = teacherService.updateTeacher(teacherId, request, authentication.getName());
        log.info("강좌의 강사 정보 변경 성공");
        return ResponseEntity.ok().body(Response.success(updatedTeacher));
    }

    // 강사 삭제
    @DeleteMapping("/{teacherId}")
    public ResponseEntity<Response<DeleteTeacherResponse>> delete(@PathVariable("teacherId") Long teacherId,
                                                                  Authentication authentication) {

        DeleteTeacherResponse deletedTeacher = teacherService.deleteTeacher(teacherId, authentication.getName());
        log.info("강좌의 강사 정보 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedTeacher));
    }
}