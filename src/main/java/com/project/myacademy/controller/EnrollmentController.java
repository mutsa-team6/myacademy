package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.domain.student.dto.ReadStudentResponse;
import com.project.myacademy.domain.waitinglist.WaitinglistService;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EnrollmentController {

    private final StudentService studentService;
    private final LectureService lectureService;
    private final WaitinglistService waitinglistService;

    @GetMapping("/academy/enrollment")
    public String studentListForEnrollment(@RequestParam(required = false) String studentName, Model model, Pageable pageable, Authentication authentication) {

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


        return "pages/enrollment";
    }

    @GetMapping("/academy/enrollment/register")
    public String lectureRegister(@RequestParam(required = false) Long studentId, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);


        ReadStudentResponse foundStudent = null;
        if (studentId != null) {
            foundStudent = studentService.readStudent(academyId, studentId, requestAccount);
        }

        Page<ReadAllLectureResponse> lectures = lectureService.readAllLecturesForEnrollment(academyId, requestAccount, pageable);
        for (ReadAllLectureResponse lecture : lectures) {
            lecture.setWaitingNum(waitinglistService.countWaitingListByLecture(academyId, lecture.getLectureId(), requestAccount));
        }
        model.addAttribute("lectures", lectures);
        model.addAttribute("student", foundStudent);
        model.addAttribute("academyId", academyId);


        return "enrollment/register";
    }
}
