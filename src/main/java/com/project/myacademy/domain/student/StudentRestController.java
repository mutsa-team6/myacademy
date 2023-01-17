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
    @GetMapping("/students/{studentsId}")
    public ResponseEntity<Response<FindStudentResponse>> find(@PathVariable Long studentsId) {
        FindStudentResponse response = studentService.findStudent(studentsId);
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
    @PutMapping("/students/{studentsId}")
    public ResponseEntity<Response<UpdateStudentResponse>> update(@PathVariable Long studentsId, UpdateStudentRequest request) {
        UpdateStudentResponse response = studentService.updateStudent(studentsId, request);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
