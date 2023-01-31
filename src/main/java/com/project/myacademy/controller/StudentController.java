package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.domain.parent.ParentService;
import com.project.myacademy.domain.parent.dto.FindParentForUIResponse;
import com.project.myacademy.domain.parent.dto.FindParentRequest;
import com.project.myacademy.domain.parent.dto.FindParentResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final AcademyService academyService;
    private final ParentService parentService;
    private final StudentService studentService;

    @GetMapping("/academy/student")
    public String student(Model model, Pageable pageable) {

        return "pages/student";
    }

    @GetMapping("/academy/student/register")
    public String studentRegister( Model model, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        model.addAttribute("academyId", academyId);

        return "student/register";
    }

    @ResponseBody
    @PostMapping("/academy/student/parentCheck")
    public FindParentForUIResponse parentCheckBeforeRegister(@RequestBody FindParentRequest request, Authentication authentication) {
        String parentPhoneNum = request.getPhoneNum();
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        log.info("🔎 찾으려는 학원 id [{}] || 부모 전화 [{}]", academyId, parentPhoneNum);

        boolean isExist = parentService.checkExistByPhoneAndAcademy(parentPhoneNum, academyId);
        FindParentForUIResponse response = new FindParentForUIResponse(isExist, academyId);

        return response;
    }

    @GetMapping("/academy/students/list")
    public String studentList(@RequestParam(required = false) String studentName, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        Page<ReadAllStudentResponse> studentList = studentService.readAllStudent(academyId, pageable, requestAccount);

        if (studentName != null) {
            List<ReadAllStudentResponse> searchStudents = studentService.findStudentForStudentList(academyId, studentName);
            model.addAttribute("students", searchStudents);

        } else {
            model.addAttribute("students", studentList);
        }
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());


        return "student/list";
    }
}
