package com.project.myacademy.domain.student;

import com.project.myacademy.domain.student.dto.*;
import com.project.myacademy.global.Response;
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
@RequestMapping("api/v1/students")
public class StudentRestController {

    private final StudentService studentService;

    /**
     * 학생 등록
     */
    @PostMapping("")
    public ResponseEntity<Response<CreateStudentResponse>> create(CreateStudentRequest request, Authentication authentication) {
        String account = authentication.getName();
        CreateStudentResponse response = studentService.createStudent(request, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 단건 조회
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<Response<FindStudentResponse>> find(@PathVariable Long studentId, Authentication authentication) {
        String account = authentication.getName();
        FindStudentResponse response = studentService.findStudent(studentId, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 전체 조회
     */
    @GetMapping("")
    public ResponseEntity<Response<Page<FindAllStudentResponse>>> findAll(Authentication authentication) {
        String account = authentication.getName();
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<FindAllStudentResponse> responses = studentService.findAllStudent(pageable,account);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 학생 정보 수정
     */
    @PutMapping("/{studentId}")
    public ResponseEntity<Response<UpdateStudentResponse>> update(@PathVariable Long studentId, UpdateStudentRequest request, Authentication authentication) {
        String account = authentication.getName();
        UpdateStudentResponse response = studentService.updateStudent(studentId, request, account);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 삭제
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Response<DeleteStudentResponse>> delete(@PathVariable Long studentId, Authentication authentication) {
        String account = authentication.getName();
        DeleteStudentResponse response = studentService.deleteStudent(studentId,account);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
