package com.project.myacademy.domain.student;

import com.project.myacademy.domain.student.dto.CreateStudentRequest;
import com.project.myacademy.domain.student.dto.CreateStudentResponse;
import com.project.myacademy.domain.student.dto.FindStudentResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
    public Response<CreateStudentResponse> create( CreateStudentRequest request) {
        //String userName = authentication.getName();
        CreateStudentResponse response = studentService.createStudent(request);
        return Response.success(response);
    }

    /**
     * 학생 정보 단건 조회
     */
    @GetMapping("/students/{studentsId}")
    public Response<FindStudentResponse> find(@PathVariable Long studentsId) {
        FindStudentResponse findStudentResponse = studentService.findStudent(studentsId);
        return Response.success(findStudentResponse);
    }
}
