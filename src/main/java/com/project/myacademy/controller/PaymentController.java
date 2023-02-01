package com.project.myacademy.controller;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.discount.DiscountService;
import com.project.myacademy.domain.discount.dto.GetDiscountResponse;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.domain.employee.dto.ReadEmployeeResponse;

import com.project.myacademy.domain.enrollment.EnrollmentService;
import com.project.myacademy.domain.enrollment.dto.FindEnrollmentResponse;
import com.project.myacademy.domain.payment.PaymentService;
import com.project.myacademy.domain.payment.dto.CompletePaymentResponse;
import com.project.myacademy.domain.payment.dto.SuccessPaymentResponse;
import com.project.myacademy.domain.student.StudentService;
import com.project.myacademy.domain.student.dto.ReadAllStudentResponse;
import com.project.myacademy.global.util.AuthenticationUtil;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final EmployeeService employeeService;
    private final EnrollmentService enrollmentService;
    private final DiscountService discountService;
    private final PaymentService paymentService;
    private final StudentService studentService;
    @Value("${payment.toss.testClientApiKey}")
    private String key;
    @Value("${payment.toss.successCallbackUrl}")
    private String successCallbackUrl;

    @Value("${payment.toss.failCallbackUrl}")
    private String failCallbackUrl;

    @GetMapping("/academy/payment/register")
    public String main(@RequestParam(required = false) String studentName, HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);

        if (studentName != null) {

            Page<FindEnrollmentResponse> enrollments = enrollmentService.findEnrollmentForPay(academyId, studentName, pageable);
            log.info("💲 결제 등록을 위한 검색 학생 이름 [{}] ", studentName);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
            model.addAttribute("next", pageable.next().getPageNumber());

        } else {
            Page<FindEnrollmentResponse> enrollments = enrollmentService.findAllEnrollmentForPay(academyId, pageable);
            model.addAttribute("enrollments", enrollments);
            model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
            model.addAttribute("next", pageable.next().getPageNumber());
        }
        log.info("🔑 key = {}", key);
        model.addAttribute("tossKey", key);
        model.addAttribute("successUrl", successCallbackUrl);
        model.addAttribute("failUrl", failCallbackUrl);

        Page<GetDiscountResponse> discounts = discountService.getAllDiscounts(academyId, requestAccount, pageable);
        model.addAttribute("discounts", discounts);


        return "payment/register";
    }

    @GetMapping("/academy/payment/success")
    public String paySuccess(@RequestParam String orderId, @RequestParam String paymentKey, @RequestParam Integer amount, HttpServletRequest request, Model model, Authentication authentication, Pageable pageable) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("💲 결제 성공한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);
        log.info("💲 결제 성공한 order id : [{}] || payment key [{}]", orderId, paymentKey);

        // 결제 성공 시, payment key 저장
        paymentService.verifyRequest(paymentKey, orderId, amount);

        SuccessPaymentResponse foundPayment = paymentService.findPayment(orderId);
        Long lectureId = foundPayment.getLectureId();
        Long studentId = foundPayment.getStudentId();
        log.info("💲 결제 성공한 lecture id : [{}] || student key [{}]", lectureId, studentId);

        FindEnrollmentResponse enrollment = enrollmentService.findEnrollmentForPaySuccess(studentId, lectureId);

        if (enrollment.getPaymentYN() == false) {
            paymentService.successApprovePayment(paymentKey, orderId, amount);
        }


        SuccessPaymentResponse payment = paymentService.findPayment(orderId);
        model.addAttribute("payment", payment);


        return "payment/success";
    }
    @GetMapping("/academy/payment")
    public String paySuccess() {

        return "pages/payment";
    }

    @GetMapping("/academy/payment/list")
    public String paymentList(@RequestParam(required = false) String studentName, Model model, Pageable pageable, Authentication authentication) {

        Long academyId = AuthenticationUtil.getAcademyIdFromAuth(authentication);
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        log.info("💲 결제 내역 조회한 사용자의 학원 id [{}] || 요청한 사용자의 계정 [{}]", academyId, requestAccount);


        if (studentName != null) {
            Page<CompletePaymentResponse> payments = paymentService.findAllCompletePaymentByStudent(academyId, requestAccount, studentName, pageable);
            model.addAttribute("payments", payments);

        } else {
            Page<CompletePaymentResponse> payments = paymentService.findAllCompletePayment(academyId, requestAccount, pageable);
            model.addAttribute("payments", payments);
        }
        model.addAttribute("previous", pageable.previousOrFirst().getPageNumber());
        model.addAttribute("next", pageable.next().getPageNumber());


        return "payment/list";
    }
}
