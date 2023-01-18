package com.project.myacademy.domain.student;

import com.project.myacademy.domain.student.dto.*;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    @PostMapping("/students")
    public ResponseEntity<Response<CreateStudentResponse>> create(CreateStudentRequest request) {
        //String userName = authentication.getName();
        CreateStudentResponse response = studentService.createStudent(request);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 단건 조회
     */
    @GetMapping("/students/{studentId}")
    public ResponseEntity<Response<FindStudentResponse>> find(@PathVariable Long studentId) {
        FindStudentResponse response = studentService.findStudent(studentId);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 학생 정보 전체 조회
     */
    @GetMapping("/students")
    public ResponseEntity<Response<Page<FindAllStudentResponse>>> findAll() {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<FindAllStudentResponse> responses = studentService.findAllStudent(pageable);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 학생 정보 수정
     */
    @PutMapping("/students/{studentId}")
    public ResponseEntity<Response<UpdateStudentResponse>> update(@PathVariable Long studentId, UpdateStudentRequest request) {
        UpdateStudentResponse response = studentService.updateStudent(studentId, request);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
