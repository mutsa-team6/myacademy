package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
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

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        // 해당 학원에 있는 강사 목록과 프로필 사진을 보여주기 위함
        Page<ReadEmployeeResponse> teachers = employeeService.findAllTeachers(requestAccount, academyId, pageable);
        for (ReadEmployeeResponse teacher : teachers) {
            teacher.setImageUrl(employeeProfileS3UploadService.getStoredUrl(teacher.getId()));
        }
        model.addAttribute("teachers", teachers);


        return "pages/lecture";
    }

    @GetMapping("/academy/lecture/register")
    public String lectureRegister(@RequestParam(required = false) Long teacherId, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        // 선택한 강사의 정보를 보여주기 위함
        ReadEmployeeResponse teacher = null;
        if (teacherId != null) {
            teacher = employeeService.findOneTeacher(requestAccount, academyId, teacherId);
            teacher.setImageUrl(employeeProfileS3UploadService.getStoredUrl(teacherId));
        }
        model.addAttribute("teacher", teacher);

        // 선택한 강사가 하는 강의 종료일이 지나지 않은 수업 목록을 가져온다.
        Page<ReadAllLectureResponse> lectures = lectureService.readAllLecturesByTeacherId(academyId, requestAccount, teacherId, pageable);
        model.addAttribute("lectures", lectures);

        return "lecture/register";
    }
    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
        model.addAttribute("academy", academy);
        return academy;
    }

    private ReadEmployeeResponse setSessionEmployeeInfo(HttpServletRequest request, Model model, Authentication authentication, Long academyId) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //view 에 회원 계정, 회원 직책 세션에 저장
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionEmployeeNameAndRole(request, employee);
        model.addAttribute("employee", employee);
        return employee;
    }
}
