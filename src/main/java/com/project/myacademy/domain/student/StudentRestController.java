package com.project.myacademy.domain.student;

import com.project.myacademy.domain.student.dto.CreateStudentRequest;
import com.project.myacademy.domain.student.dto.CreateStudentResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        log.info("Student phoneNum : " + request.getPhoneNum());

        //String userName = authentication.getName();
        CreateStudentResponse response = studentService.createStudent(request);
        return Response.success(response);
    }
}
