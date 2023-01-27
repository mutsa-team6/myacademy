package com.project.myacademy.domain.student;

import com.project.myacademy.domain.student.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class StudentRestController {

    private final StudentService studentService;

    /**
     * 학생 등록
     */
    @PostMapping("/{academyId}/students")
    public ResponseEntity<Response<CreateStudentResponse>> create(@PathVariable Long academyId, CreateStudentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateStudentResponse response = studentService.createStudent(academyId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 단건 조회
     */
    @GetMapping("/{academyId}/students/{studentId}")
    public ResponseEntity<Response<ReadStudentResponse>> read(@PathVariable Long academyId, @PathVariable Long studentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        ReadStudentResponse response = studentService.readStudent(academyId, studentId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 전체 조회
     */
    @GetMapping("/{academyId}/students")
    public ResponseEntity<Response<Page<ReadAllStudentResponse>>> readAll(@PathVariable Long academyId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllStudentResponse> responses = studentService.readAllStudent(academyId, pageable, requestAccount);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 학생 정보 수정
     */
    @PutMapping("/{academyId}/students/{studentId}")
    public ResponseEntity<Response<UpdateStudentResponse>> update(@PathVariable Long academyId, @PathVariable Long studentId, UpdateStudentRequest request, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateStudentResponse response = studentService.updateStudent(academyId,studentId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 삭제
     */
    @DeleteMapping("/{academyId}/students/{studentId}")
    public ResponseEntity<Response<DeleteStudentResponse>> delete(@PathVariable Long academyId, @PathVariable Long studentId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteStudentResponse response = studentService.deleteStudent(academyId, studentId,requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
