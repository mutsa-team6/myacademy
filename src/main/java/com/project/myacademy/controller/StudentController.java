package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyForUIResponse;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.parent.ParentService;
import com.project.myacademy.domain.parent.dto.FindParentForUIResponse;
import com.project.myacademy.domain.parent.dto.FindParentRequest;
import com.project.myacademy.domain.payment.PaymentService;
import com.project.myacademy.domain.payment.dto.CompletePaymentResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.domain.student.dto.ReadStudentResponse;
import com.project.myacademy.domain.uniqueness.UniquenessService;
import com.project.myacademy.domain.uniqueness.dto.ReadAllUniquenessResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
import com.project.myacademy.global.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StudentController {

    private final AcademyService academyService;
    private final ParentService parentService;
    private final StudentService studentService;

    private final LectureService lectureService;
    private final EnrollmentService enrollmentService;
    private final PaymentService paymentService;
    private final UniquenessService uniquenessService;
    private final EmployeeService employeeService;

    @GetMapping("/academy/student")
    public String student(HttpServletRequest request, Authentication authentication, Model model, Pageable pageable) {
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);

        return "pages/student";
    }

    @GetMapping("/academy/student/register")
    public String studentRegister(HttpServletRequest request, Model model, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);

        return "student/register";
    }

    @ResponseBody
    @PostMapping("/academy/student/parentCheck")
    public FindParentForUIResponse parentCheckBeforeRegister(@RequestBody FindParentRequest request, Authentication authentication) {
        String parentPhoneNum = request.getPhoneNum();
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        FindParentForUIResponse response = new FindParentForUIResponse(false, 0L);

        boolean isExist = parentService.checkExistByPhoneAndAcademy(parentPhoneNum, academyId);
        if (isExist) {
            response = new FindParentForUIResponse(isExist, academyId);
        }

        return response;
    }

    @GetMapping("/academy/students/list")
    public String studentList(@RequestParam(required = false) String studentName, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        if (studentName != null) {
            Page<ReadAllStudentResponse> searchStudents = studentService.findStudentForStudentList(academyId, studentName, pageable);
            model.addAttribute("students", searchStudents);

        } else {
            Page<ReadAllStudentResponse> studentList = studentService.readAllStudent(academyId, pageable, requestAccount);
            model.addAttribute("students", studentList);
        }

        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());


        return "student/list";
    }

    @GetMapping("/academy/student/info")
    public String lectureRegister(@RequestParam Long studentId, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // 직원 정보, 학원 정보 세션에 저장 및 model로 넘기는 메서드
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        // 학생 상세 정보
        ReadStudentResponse student = studentService.readStudent(academyId, studentId, requestAccount);
        model.addAttribute("student", student);

        // 결제가 완료된 학생이 수강한 강의 내역 보여주기
        Page<FindEnrollmentResponse> enrollments = enrollmentService.findEnrollmentByStudentId(academyId, studentId, pageable);
        model.addAttribute("enrollments", enrollments);


        // 학생의 결제 완료된 내역 보여주기
        Page<CompletePaymentResponse> payments = paymentService.findAllCompletePaymentByStudent(academyId, requestAccount, studentId, pageable);
        model.addAttribute("payments", payments);


        // 학생 특이 사항
        Page<ReadAllUniquenessResponse> uniquenesses = uniquenessService.readAllUniqueness(academyId, studentId, pageable, requestAccount);
        model.addAttribute("uniquenesses", uniquenesses);
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());

        return "student/info";
    }

    private FindAcademyResponse setSessionAcademyInfo(HttpServletRequest request, Model model, Long academyId) {
        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        SessionUtil.setSessionAcademyName(request, academy);
        model.addAttribute("academy", academy);
        model.addAttribute("localDateTime", LocalDateTime.now());

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
