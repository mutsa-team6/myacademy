package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.teacher.dto.*;
import com.project.myacademy.global.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "07-1. 강사", description = "강사 등록,수정,조회")
@RestController
@RequestMapping("api/v1/academies")
@RequiredArgsConstructor
@Slf4j
public class TeacherRestController {

    private final TeacherService teacherService;

    // 강사 정보 등록
    @Operation(summary = "강사 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다.")
    @PostMapping("/{academyId}/employees/{employeeId}/teachers")
    public ResponseEntity<Response<CreateTeacherResponse>> create(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("employeeId") Long employeeId,
                                                                  @RequestBody CreateTeacherRequest request,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateTeacherResponse createdTeacher = teacherService.createTeacher(academyId, employeeId, request, account);
        log.info("강사 배정 성공");
        return ResponseEntity.ok().body(Response.success(createdTeacher));
    }

    // 강사 정보 수정
    @Operation(summary = "강사 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/teachers/{teacherId}")
    public ResponseEntity<Response<UpdateTeacherResponse>> update(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  @RequestBody UpdateTeacherRequest request,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateTeacherResponse updatedTeacher = teacherService.updateTeacher(academyId, teacherId, request, account);
        log.info("강사 정보 변경 성공");
        return ResponseEntity.ok().body(Response.success(updatedTeacher));
    }

    // 강사 삭제
    @Operation(summary = "강사 삭제", description = "ADMIN,STAFF 회원만 수정이 가능합니다. \n\n soft-delete 됩니다.")
    @DeleteMapping("/{academyId}/teachers/{teacherId}")
    public ResponseEntity<Response<DeleteTeacherResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                  @PathVariable("teacherId") Long teacherId,
                                                                  Authentication authentication) {
        String account = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteTeacherResponse deletedTeacher = teacherService.deleteTeacher(academyId, teacherId, account);
        log.info("강사 정보 삭제 성공");
        return ResponseEntity.ok().body(Response.success(deletedTeacher));
    }
}