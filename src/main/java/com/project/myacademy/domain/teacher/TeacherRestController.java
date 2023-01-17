package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.teacher.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class TeacherRestController {

    private final TeacherService teacherService;

    // 강사 정보 등록
    @PostMapping("/{academyId}/employees/{employeeId}/teachers")
    public ResponseEntity<Response<CreateTeacherResponse>> create(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("employeeId") Long employeeId,
                                                                  @RequestBody CreateTeacherRequest request) {
        CreateTeacherResponse createdTeacher = teacherService.createTeacher(academyId, employeeId, request);
        log.info("강좌의 강사 배정 성공");
        return ResponseEntity.ok().body(Response.success(createdTeacher));
    }

    // 강사 정보 수정
    @PutMapping("/{academyId}/teachers/{teacherId}")
    public ResponseEntity<Response<UpdateTeacherResponse>> update(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @RequestBody UpdateTeacherRequest request) {

        UpdateTeacherResponse updatedTeacher = teacherService.updateTeacher(academyId, teacherId, request);
        log.info("강좌의 강사 정보 변경 성공");
        return ResponseEntity.ok().body(Response.success(updatedTeacher));
    }

    // 강사 삭제
    @DeleteMapping("/{academyId}/teachers/{teacherId}")
    public ResponseEntity<Response<DeleteTeacherResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId) {

        DeleteTeacherResponse deletedTeacher = teacherService.deleteTeacher(academyId, teacherId);
        log.info("강좌의 강사 정보 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedTeacher));
    }
}