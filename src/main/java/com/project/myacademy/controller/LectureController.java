package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import com.project.myacademy.global.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LectureController {
    private final EmployeeService employeeService;
    private final AcademyService academyService;

    private final LectureService lectureService;
    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;

    @GetMapping("/academy/lecture")
    public String main(HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("⭐ 강좌 등록 요청한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request,employee);

        Page<ReadEmployeeResponse> teachers = employeeService.findAllTeachers(requestAccount, academyId, pageable);
        for (ReadEmployeeResponse teacher : teachers) {
            teacher.setImageUrl(employeeProfileS3UploadService.getStoredUrl(teacher.getId()));
        }
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("teachers", teachers);
        model.addAttribute("account", requestAccount);

        return "pages/lecture";
    }

    @GetMapping("/academy/lecture/register")
    public String lectureRegister(@RequestParam(required = false) Long teacherId, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request,employee);

        ReadEmployeeResponse teacher = null;
        if (teacherId != null) {
            teacher = employeeService.findOneTeacher(requestAccount, academyId, teacherId);
            teacher.setImageUrl(employeeProfileS3UploadService.getStoredUrl(teacherId));
        }

        Page<ReadAllLectureResponse> lectures = lectureService.readAllLecturesByTeacherId(academyId, requestAccount, teacherId, pageable);
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);
        model.addAttribute("teacher", teacher);
        model.addAttribute("lectures", lectures);
        model.addAttribute("academyId", academyId);
        model.addAttribute("teacherEmployeeId", teacher.getId());


        return "lecture/register";
    }

}
