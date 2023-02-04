package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyRequest;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.academy.dto.ReadAcademyResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;
import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.domain.lecture.LectureService;
import com.project.myacademy.domain.lecture.dto.ReadAllLectureResponse;
import com.project.myacademy.domain.parent.ParentService;
import com.project.myacademy.domain.parent.dto.FindParentForUIResponse;
import com.project.myacademy.domain.parent.dto.FindParentRequest;
import com.project.myacademy.domain.parent.dto.FindParentResponse;
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
import java.util.List;

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
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);


        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("account", requestAccount);

        return "pages/student";
    }

    @GetMapping("/academy/student/register")
    public String studentRegister(HttpServletRequest request, Model model, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        FindAcademyResponse academy = academyService.findAcademyById(academyId);
        model.addAttribute("academy", academy);
        model.addAttribute("academyId", academyId);
        model.addAttribute("account", requestAccount);

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
    public String studentList(@RequestParam(required = false) String studentName,HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        if (studentName != null) {
            Page<ReadAllStudentResponse> searchStudents = studentService.findStudentForStudentList(academyId, studentName, pageable);
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


        return "student/list";
    }

    @GetMapping("/academy/student/info")
    public String lectureRegister(@RequestParam Long studentId,HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        //회원 이름 표시
        ReadEmployeeResponse employee = employeeService.readEmployee(academyId, requestAccount);
        SessionUtil.setSessionNameAndRole(request, employee);

        ReadStudentResponse student = studentService.readStudent(academyId, studentId, requestAccount);

        Page<FindEnrollmentResponse> enrollments = enrollmentService.findEnrollmentByStudentId(academyId, studentId, pageable);

        Page<CompletePaymentResponse> payments = paymentService.findAllCompletePaymentByStudent(academyId, requestAccount, studentId, pageable);

        FindAcademyResponse academy = academyService.findAcademyById(academyId);

        Page<ReadAllUniquenessResponse> uniquenesses = uniquenessService.readAllUniqueness(academyId, studentId, pageable, requestAccount);

        model.addAttribute("uniquenesses", uniquenesses);
        model.addAttribute("payments", payments);
        model.addAttribute("academy", academy);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("student", student);
        model.addAttribute("account", requestAccount);
        model.addAttribute("academyId", academyId);


        return "student/info";
    }

}
