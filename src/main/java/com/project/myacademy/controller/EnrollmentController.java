package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.domain.student.dto.ReadStudentResponse;
import com.project.myacademy.domain.waitinglist.WaitinglistService;
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
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EnrollmentController {

    private final StudentService studentService;
    private final LectureService lectureService;
    private final WaitinglistService waitinglistService;
    private final AcademyService academyService;
    private final EmployeeService employeeService;

    private final EnrollmentService enrollmentService;

    @GetMapping("/academy/enrollment")
    public String studentListForEnrollment(@RequestParam(required = false) String studentName, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request,employee);


        if (studentName != null) {
            Page<ReadAllStudentResponse> searchStudents = studentService.findStudentForStudentList(academyId, studentName,pageable);
            model.addAttribute("students", searchStudents);

        } else {
            Page<ReadAllStudentResponse> studentList = studentService.readAllStudent(academyId, pageable, requestAccount);
            model.addAttribute("students", studentList);
        }
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());


        return "pages/enrollment";
    }

    @GetMapping("/academy/enrollment/register")
    public String lectureRegister(@RequestParam(required = false) Long studentId,HttpServletRequest request ,Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request,employee);

        ReadStudentResponse foundStudent = null;
        if (studentId != null) {
            foundStudent = studentService.readStudent(academyId, studentId, requestAccount);
        }

        Page<ReadAllLectureResponse> lectures = lectureService.readAllLecturesForEnrollment(academyId, requestAccount, pageable);
        for (ReadAllLectureResponse lecture : lectures) {
            lecture.setWaitingNum(waitinglistService.countWaitingListByLecture(academyId, lecture.getLectureId(), requestAccount));
            lecture.setRegisteredStudent(enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academyId, requestAccount, lecture.getLectureId()));
            lecture.setWaitingStduent(waitinglistService.findWaitingStudentByLecture(academyId, lecture.getLectureId(), requestAccount));
        }
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);
        model.addAttribute("lectures", lectures);
        model.addAttribute("student", foundStudent);
        model.addAttribute("academyId", academyId);


        return "enrollment/register";
    }
}
