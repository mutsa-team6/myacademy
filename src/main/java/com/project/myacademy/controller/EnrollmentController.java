package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
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
import java.time.LocalDateTime;

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

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        // ???????????????, ?????? ???????????? ???????????? ??????
        if (studentName != null) {
            Page<ReadAllStudentResponse> searchStudents = studentService.findStudentForStudentList(academyId, studentName, pageable);
            model.addAttribute("students", searchStudents);

        } else {
            Page<ReadAllStudentResponse> studentList = studentService.readAllStudent(academyId, pageable, requestAccount);
            model.addAttribute("students", studentList);
        }

        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());


        return "pages/enrollment";
    }

    @GetMapping("/academy/enrollment/register")
    public String lectureRegister(@RequestParam(required = false) Long studentId,HttpServletRequest request ,Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        ReadStudentResponse foundStudent = null;

        // ?????? ?????? ??????, ????????? ?????? ????????? ???????????? ??????
        if (studentId != null) {
            foundStudent = studentService.readStudent(academyId, studentId, requestAccount);
        }
        model.addAttribute("student", foundStudent);

        // ?????? ???????????? ????????? ?????? ?????? ????????? ????????????.
        Page<ReadAllLectureResponse> lectures = lectureService.readAllLecturesForEnrollment(academyId, requestAccount, pageable);

        // ?????? ????????? ?????? ????????? ?????????, ?????? ????????? ???????????????, ?????? ????????? ??????????????? ???????????? ??????
        for (ReadAllLectureResponse lecture : lectures) {
            lecture.setWaitingNum(waitinglistService.countWaitingListByLecture(academyId, lecture.getLectureId(), requestAccount));
            lecture.setRegisteredStudent(enrollmentService.findAllStudentInfoFromEnrollmentByLecture(academyId, requestAccount, lecture.getLectureId()));
            lecture.setWaitingStudent(waitinglistService.findWaitingStudentByLecture(academyId, lecture.getLectureId(), requestAccount));
        }
        model.addAttribute("lectures", lectures);

        return "enrollment/register";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request,academy);
        model.addAttribute("academy", academy);
        model.addAttribute("localDateTime", LocalDateTime.now());

        return academy;
    }

    private ReadEmployeeResponse setSessionEmployeeInfo(HttpServletRequest request, Model model, Authentication authentication, Long academyId) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        //view ??? ?????? ??????, ?????? ?????? ????????? ??????
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionEmployeeNameAndRole(request, employee);
        model.addAttribute("employee", employee);
        return employee;
    }
}
