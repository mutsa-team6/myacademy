package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.AcademyService;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
import com.project.myacademy.domain.discount.DiscountService;
import com.project.myacademy.domain.discount.dto.GetDiscountResponse;
import com.project.myacademy.domain.email.EmailService;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;

import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.domain.payment.PaymentService;
import com.project.myacademy.domain.payment.dto.CompletePaymentResponse;
import com.project.myacademy.domain.payment.dto.SuccessPaymentResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.global.util.AuthenticationUtil;
import com.project.myacademy.global.util.SessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final EmployeeService employeeService;
    private final AcademyService academyService;
    private final EnrollmentService enrollmentService;
    private final DiscountService discountService;
    private final PaymentService paymentService;
    private final StudentService studentService;
    private final EmailService emailService;
    @Value("${payment.toss.testClientApiKey}")
    private String key;
    @Value("${payment.toss.successCallbackUrl}")
    private String successCallbackUrl;

    @Value("${payment.toss.failCallbackUrl}")
    private String failCallbackUrl;

    private static final String PREVIOUS = "previous";
    private static final String NEXT = "next";

    @GetMapping("/academy/payment/register")
    public String main(@RequestParam(required = false) String studentName, HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();

        if (studentName != null) {

            Page<FindEnrollmentResponse> enrollments = enrollmentService.findEnrollmentForPay(academyId, studentName, pageable);
            log.info("???? ?????? ????????? ?????? ?????? ?????? ?????? [{}] ", studentName);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
            model.addAttribute(NEXT, pageable.next().getPageNumber());

        } else {
            Page<FindEnrollmentResponse> enrollments = enrollmentService.findAllEnrollmentForPay(academyId, pageable);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
            model.addAttribute(NEXT, pageable.next().getPageNumber());
        }
        log.info("???? key = {}", key);
        model.addAttribute("tossKey", key);
        model.addAttribute("successUrl", successCallbackUrl);
        model.addAttribute("failUrl", failCallbackUrl);

        Page<GetDiscountResponse> discounts = discountService.getAllDiscounts(academyId, requestAccount, pageable);
        model.addAttribute("discounts", discounts);
        model.addAttribute("account", requestAccount);

        return "payment/register";
    }

    @GetMapping("/academy/payment/success")
    public String paySuccess(@RequestParam String orderId, @RequestParam String paymentKey, @RequestParam Integer amount, HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) throws MessagingException {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);

        // ?????? ?????? ???, payment key ??????
        paymentService.verifyRequest(paymentKey, orderId, amount);

        SuccessPaymentResponse foundPayment = paymentService.findPayment(orderId);
        Long lectureId = foundPayment.getLectureId();
        Long studentId = foundPayment.getStudentId();
        log.info("???? ?????? ????????? lecture id : [{}] || student key [{}]", lectureId, studentId);

        FindEnrollmentResponse enrollment = enrollmentService.findEnrollmentForPaySuccess(studentId, lectureId);

        if (enrollment.getPaymentYN().equals(false)) {
            paymentService.successApprovePayment(paymentKey, orderId, amount);
        }

        SuccessPaymentResponse payment = paymentService.findPayment(orderId);
        model.addAttribute("payment", payment);

        String title = String.format("%s?????? %s ?????? ?????? ?????? ???????????????.",enrollment.getStudentName(),enrollment.getLectureName());
        String body = String.format("%s?????? %s ?????? %d??? ?????? ?????????????????????. \n \n???????????????.",enrollment.getStudentName(),enrollment.getLectureName(),amount);
        emailService.sendEmail(academyId, enrollment.getStudentEmail(), title, body, requestAccount);

        return "payment/success";
    }
    @GetMapping("/academy/payment")
    public String paySuccess(HttpServletRequest request,Authentication authentication,Model model) {
        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);


        return "pages/payment";
    }

    @GetMapping("/academy/payment/list")
    public String paymentList(@RequestParam(required = false) String studentName, HttpServletRequest request, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);

        // ?????? ??????, ?????? ?????? ????????? ?????? ??? model??? ????????? ?????????
        ReadEmployeeResponse requestEmployee = setSessionEmployeeInfo(request, model, authentication, academyId);
        setSessionAcademyInfo(request, model, academyId);
        String requestAccount = requestEmployee.getAccount();


        //?????? ???????????? ?????? ?????? ??????
        if (studentName != null) {
            Page<CompletePaymentResponse> payments = paymentService.findAllCompletePaymentByStudent(academyId, requestAccount, studentName, pageable);
            model.addAttribute("payments", payments);

        } else {
            Page<CompletePaymentResponse> payments = paymentService.findAllCompletePayment(academyId, requestAccount, pageable);
            model.addAttribute("payments", payments);
        }

        model.addAttribute(PREVIOUS, pageable.previousOrFirst().getPageNumber());
        model.addAttribute(NEXT, pageable.next().getPageNumber());


        return "payment/list";
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
