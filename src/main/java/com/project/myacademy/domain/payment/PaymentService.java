package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.payment.dto.PaymentRequest;
import com.project.myacademy.domain.payment.dto.PaymentResponse;
import com.project.myacademy.domain.payment.dto.PaymentResponseHandleDto;
import com.project.myacademy.domain.payment.dto.PaymentResponseHandleFailDto;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AcademyRepository academyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;

    @Value("${payment.toss.test_client_api_key}")
    private String testClientApiKey;

    @Value("${payment.toss.test_secret_api_key}")
    private String testSecretApiKey;

    @Value("${payment.toss.success_url}")
    private String successCallBackUrl;

    @Value("${payment.toss.fail_url}")
    private String failCallBackUrl;

    @Value("${payment.toss.origin_url}")
    private String tossOriginUrl;

    /**
     * 결제 요청 검증
     * @param request
     * @param academyId
     * @param studentId
     * @param account
     */
    @Transactional
    public PaymentResponse requestPayments(PaymentRequest request, Long academyId, Long studentId, String account) {
        //학원이 존재하는지 여부
        Academy academy = validateAcademy(academyId);

        //학원에 근무하는 직원이 맞는지 확인
        Employee foundEmployee = validateAcademyEmployee(account, academy);

        //결제할 학생 조회
        Student foundStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        //결제할 학생이 수강신청한 수업
        Enrollment studentEnrollment = enrollmentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        Long amount = request.getAmount();
        String payType = request.getPayType().getName();
        String orderName = request.getOrderName();

        //가격 검증
        if (!amount.equals(studentEnrollment.getLecture().getPrice())) {
            throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
        }

        //결제 방법 검증
        if (!payType.equals("카드") && !payType.equals("CARD")) {
            throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PAY_TYPE);
        }

        //주문 이름 검증
        if (!orderName.equals(studentEnrollment.getLecture().getName())) {
            throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_NAME);
        }

        PaymentResponse response;
        Payment payment = request.toEntity(foundEmployee,foundStudent);

        response = payment.toDto(foundEmployee,foundStudent);
        response.setSuccessUrl(successCallBackUrl);
        response.setFailUrl(failCallBackUrl);

        return response;

//        Payment savedPayment = paymentRepository.save(new PaymentRequest().toEntity(foundEmployee,foundStudent));
//
//        PaymentResponse response = PaymentResponse.builder()
//                .amount(savedPayment.getAmount())
//                .payType(savedPayment.getPayType())
//                .orderId(savedPayment.getOrderId())
//                .orderName(savedPayment.getOrderName())
//                .studentName(savedPayment.getStudent().getName())
//                .studentEmail(savedPayment.getStudent().getEmail())
//                .successUrl(successCallBackUrl)
//                .failUrl(failCallBackUrl)
//                .build();
    }

    /**
     *
     * @param paymentKey
     * @param orderId
     * @param amount
     */
    @Transactional
    public void verifyRequest(String paymentKey, String orderId, Long amount) {
        paymentRepository.findByOrderId(orderId).ifPresentOrElse(
                p->{
                    if (p.getAmount().equals(amount)) {
                        p.setPaymentKey(paymentKey);
                    } else {
                        throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
                    }
                },()->{
                    throw new AppException(ErrorCode.PAYMENT_REQUIRED);
                }
        );
    }

    /**
     *
     * @param paymentKey
     * @param orderId
     * @param amount
     * @return
     */
    @Transactional
    public PaymentResponseHandleDto requestFinalPayment(String paymentKey, String orderId, Long amount) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        testSecretApiKey = testSecretApiKey + ":";
        String encodedAuth = new String(Base64.getEncoder().encode(testSecretApiKey.getBytes(StandardCharsets.UTF_8)));

        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        JSONObject param = new JSONObject();
        param.put("orderId", orderId);
        param.put("amount", amount);

        return rest.postForEntity(
                        tossOriginUrl+paymentKey,
                        new HttpEntity<>(param,headers),
                        PaymentResponseHandleDto.class)
                .getBody();
    }

    /**
     *
     * @param errorCode
     * @param errorMsg
     * @param orderId
     * @return
     */
    public PaymentResponseHandleFailDto requestFail(String errorCode, String errorMsg, String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponseHandleFailDto.builder()
                .orderId(orderId)
                .errorCode(ErrorCode.PAYMENT_NOT_FOUND)
                .errorMsg(ErrorCode.PAYMENT_NOT_FOUND.getMessage())
                .build();
    }

    private Academy validateAcademy(Long academyId) {
        // 학원 존재 유무 확인
        Academy validatedAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validatedAcademy;
    }
    private Employee validateAcademyEmployee(String account, Academy academy) {
        // 해당 학원 소속 직원 맞는지 확인
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return employee;
    }

}
