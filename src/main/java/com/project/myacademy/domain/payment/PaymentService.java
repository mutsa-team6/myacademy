package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.discount.Discount;
import com.project.myacademy.domain.discount.DiscountRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.payment.dto.*;
import com.project.myacademy.domain.payment.entity.CancelPayment;
import com.project.myacademy.domain.payment.entity.Payment;
import com.project.myacademy.domain.payment.repository.CancelPaymentRepository;
import com.project.myacademy.domain.payment.repository.PaymentRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AcademyRepository academyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final LectureRepository lectureRepository;
    private final CancelPaymentRepository cancelPaymentRepository;
    private final DiscountRepository discountRepository;

    @Value("${payment.toss.testSecretApiKey}")
    private String testSecretApiKey;

    @Value("${payment.toss.originUrl}")
    private String tossOriginUrl;

    @Value("${payment.toss.successCallbackUrl}")
    private String successCallbackUrl;

    @Value("${payment.toss.failCallbackUrl}")
    private String failCallbackUrl;

    /**
     * 결제할 상품 가격,지불 방법, 수업 이름 체크
     *
     * @param request   수업 가격, 지불방법, 수업이름
     * @param academyId
     * @param studentId
     * @param account
     */
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request, Long academyId, Long studentId, String account) {
        //학원이 존재하는지 여부
        Academy academy = validateAcademy(academyId);

        //학원에 근무하는 직원이 맞는지 확인
        Employee foundEmployee = validateAcademyEmployee(account, academy);

        //결제할 학생 조회
        Student foundStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Lecture foundLecture = lectureRepository.findById(request.getLectureId())
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        //결제할 학생이 수강신청한 수업
        Enrollment studentEnrollment = enrollmentRepository.findByStudentAndLecture(foundStudent, foundLecture)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        //수강 결제가 된거면 중복 결제안되도록 막기
        if (studentEnrollment.getPaymentYN() != false) {
            throw new AppException(ErrorCode.DUPLICATED_PAYMENT);
        }

        Integer amount = request.getAmount();
        String payType = request.getPayType().getName();
        String orderName = request.getOrderName();


        //결제 방법 검증
        if (!payType.equals("카드") && !payType.equals("CARD")) {
            throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PAY_TYPE);
        }

        //주문 이름 검증
        if (!orderName.equals(studentEnrollment.getLecture().getName())) {
            throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_NAME);
        }

        //가격 검증
        if (request.getDiscountId() != 0) {
            Discount foundDiscount = discountRepository.findById(request.getDiscountId())
                    .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));

            Float discountRate = (100 - foundDiscount.getDiscountRate()) / 100f;
            Integer discountAmount = Math.round(studentEnrollment.getLecture().getPrice() * discountRate);

            log.info("💰 수업 정가 = {}", studentEnrollment.getLecture().getPrice());
            log.info("💰 할인률 = {}", discountRate);
            log.info("💰 할인된 수업 가격 = {}", discountAmount);
            log.info("💰 요청 가격 = {}", amount);

            if (!amount.equals(discountAmount)) {
                throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
            }

        } else {
            if (!amount.equals(studentEnrollment.getLecture().getPrice())) {
                throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
            }
        }

        //저장
        Payment savedPayment = paymentRepository.save(request.toEntity(foundEmployee, foundStudent, studentEnrollment, academy));

        CreatePaymentResponse response = CreatePaymentResponse.of(savedPayment);
        response.setSuccessUrl(successCallbackUrl);
        response.setFailUrl(failCallbackUrl);

        return response;
    }

    /**
     * 결제 승인 전 검증과정
     *
     * @param paymentKey 토스 측 결제 고유 ID
     * @param orderId    우리측 주문 ID
     * @param amount     금액
     */
    @Transactional
    public void verifyRequest(String paymentKey, String orderId, Integer amount) {
        paymentRepository.findByOrderId(orderId).ifPresentOrElse(p -> {
                    if (p.getAmount().equals(amount)) {
                        p.setPaymentKey(paymentKey);
                        log.info("paymentKey = {}", p.getPaymentKey());
                    } else {
                        throw new AppException(ErrorCode.PAYMENT_ERROR_ORDER_PRICE);
                    }
                }
                , () -> {
                    throw new AppException(ErrorCode.PAYMENT_REQUIRED);
                }
        );
    }

    /**
     * 토스 측에 최종 결제 승인 요청
     *
     * @param paymentKey
     * @param orderId
     * @param amount
     * @return
     */
    @Transactional
    public ApprovePaymentResponse successApprovePayment(String paymentKey, String orderId, Integer amount) {
        //이미 결제되있는지 확인
        Payment selcetedPayment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findByStudentAndLecture(selcetedPayment.getStudent(), selcetedPayment.getLecture())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        if (enrollment.getPaymentYN() == true) {
            throw new AppException(ErrorCode.ALREADY_PAYMENT);
        }

        //결제 여부 false로 변경
        enrollment.updatePaymentTrue();

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        testSecretApiKey = testSecretApiKey + ":";
        String encodedAuth = new String(Base64.getEncoder().encode(testSecretApiKey.getBytes(StandardCharsets.UTF_8)));

        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        JSONObject param = new JSONObject();
        param.put("paymentKey", paymentKey);
        param.put("amount", amount);
        param.put("orderId", orderId);

        //url로 요청
        return rest.postForEntity(
                        tossOriginUrl + "confirm",
                        new HttpEntity<>(param, headers),
                        ApprovePaymentResponse.class)
                .getBody();
    }

    /**
     * 토스 측에 최종 결제 실패 요청
     *
     * @param errorCode
     * @param errorMsg
     * @param orderId
     * @return
     */
    public FailApprovePaymentResponse failApprovePayment(String errorCode, String errorMsg, String orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return FailApprovePaymentResponse.builder()
                .orderId(orderId)
                .errorCode(errorCode)
                .errorMsg(errorMsg)
                .build();
    }

    /**
     * 결제 취소
     *
     * @param paymentKey   토스측 결제 키
     * @param cancelReason 결제 취소 사유
     * @return
     */
    public ApprovePaymentResponse cancelPayment(String paymentKey, String cancelReason, String account, Long academyId) {
        //학원이 존재하는지 여부
        Academy academy = validateAcademy(academyId);

        //학원에 근무하는 직원이 맞는지 확인
        Employee foundEmployee = validateAcademyEmployee(account, academy);

        //payment 결제된 내역이 있는지 확인
        Payment selcetedPayment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Enrollment enrollment = enrollmentRepository.findByStudentAndLecture(selcetedPayment.getStudent(), selcetedPayment.getLecture())
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        //결제 여부 false로 변경
        enrollment.updatePaymentFalse();

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        URI uri = URI.create(tossOriginUrl + paymentKey + "/cancel");

        testSecretApiKey = testSecretApiKey + ":";
        String encodedAuth = new String(Base64.getEncoder().encode(testSecretApiKey.getBytes(StandardCharsets.UTF_8)));

        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        JSONObject param = new JSONObject();
        param.put("cancelReason", cancelReason);

        //cancelPayment 저장
        cancelPaymentRepository.save(CancelPayment.builder()
                .orderId(selcetedPayment.getOrderId())
                .paymentKey(paymentKey)
                .cancelReason(cancelReason)
                .amount(selcetedPayment.getAmount())
                .orderName(selcetedPayment.getOrderName())
                .payment(selcetedPayment)
                .employee(foundEmployee)
                .build());

        return rest.postForEntity(
                        uri,
                        new HttpEntity<>(param, headers),
                        ApprovePaymentResponse.class)
                .getBody();
    }

    /**
     * 결제 성공 후, 결제 정보를 보여주기 위해 만든 메서드 (UI 용)
     */
    public SuccessPaymentResponse findPayment(String orderId) {
        Payment foundPayment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Long discountId = foundPayment.getDiscountId();
        log.info("💲 결제 완료 후 discountId [{}]", discountId);

        String discountName = null;

        if (discountId == 0) {
            discountName = "할인 정책 선택 안함";
        } else {
            Discount foundDiscount = discountRepository.findById(discountId)
                    .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
            discountName = foundDiscount.getDiscountName();
        }

        return new SuccessPaymentResponse(foundPayment, discountName);

    }

    /**
     * 해당 학원의 결제 완료한 내역들을 가져오기 위해 만든 메서드 (UI 용)
     */
    public Page<CompletePaymentResponse> findAllCompletePayment(Long academyId, String requestAccount, Pageable pageable) {

        //학원 체크
        Academy foundAcademy = validateAcademy(academyId);
        //요청한 직원 체크
        validateAcademyEmployee(requestAccount, foundAcademy);

        List<CompletePaymentResponse> foundPayments = new ArrayList<>();

        // paymentkey 값이 존재하는(결제가 완료된것) payment 가져오기
        Page<Payment> payments = paymentRepository.findByAcademy_IdAndPaymentKeyIsNotNullOrderByCreatedAtDesc(academyId, pageable);
        for (Payment payment : payments) {
            CompletePaymentResponse completePayment = new CompletePaymentResponse(payment);

            discountRepository.findById(payment.getDiscountId()).ifPresent(
                    discount -> {
                        completePayment.setDiscountName(discount.getDiscountName());
                    }
            );

            Optional<CancelPayment> foundCancelPayment = cancelPaymentRepository.findByPayment(payment);
            foundCancelPayment.ifPresent(cancelPayment -> completePayment.setDeletedAt(cancelPayment));

            foundPayments.add(completePayment);
        }


        return new PageImpl<>(foundPayments);
    }

    /**
     * 해당 학원에 특정 학생의 결제 내역을 가져오기 위해 만든 메서드 // 학생 이름 검색용 메서드(UI 용)
     */
    public Page<CompletePaymentResponse> findAllCompletePaymentByStudent(Long academyId, String requestAccount, String studentName, Pageable pageable) {

        //학원 체크
        Academy foundAcademy = validateAcademy(academyId);
        //요청한 직원 체크
        validateAcademyEmployee(requestAccount, foundAcademy);

        Page<Student> foundStudents = studentRepository.findByAcademyIdAndName(academyId, studentName, pageable);

        // 아래 컬렉션에 정보를 담을 것임
        List<CompletePaymentResponse> foundPayments = new ArrayList<>();

        // 동명이인 학생이 있을 수 있어서..
        for (Student foundStudent : foundStudents) {
            List<Payment> foundPaymentsByStudent = paymentRepository.findByAcademy_IdAndPaymentKeyIsNotNullAndStudentOrderByCreatedAtDesc(academyId, foundStudent);
            for (Payment payment : foundPaymentsByStudent) {
                CompletePaymentResponse completePayment = new CompletePaymentResponse(payment);

                discountRepository.findById(payment.getDiscountId()).ifPresent(
                        discount -> {
                            completePayment.setDiscountName(discount.getDiscountName());
                        }
                );

                Optional<CancelPayment> foundCancelPayment = cancelPaymentRepository.findByPayment(payment);
                foundCancelPayment.ifPresent(cancelPayment -> completePayment.setDeletedAt(cancelPayment));

                foundPayments.add(completePayment);
            }
        }


        return new PageImpl<>(foundPayments);
    }

    /**
     * 해당 학원에 특정 학생 (id로 찾기) 의 결제 완료 내역을 가져오기 위해 만든 메서드 학생 상세페이지용 (UI 용)
     */
    public Page<CompletePaymentResponse> findAllCompletePaymentByStudent(Long academyId, String requestAccount, Long studentId, Pageable pageable) {

        //학원 체크
        Academy foundAcademy = validateAcademy(academyId);
        //요청한 직원 체크
        validateAcademyEmployee(requestAccount, foundAcademy);

        //학생 유효성 검사
        Student foundStudent = studentRepository.findByAcademyIdAndId(academyId, studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        // 아래 컬렉션에 정보를 담을 것임
        List<CompletePaymentResponse> foundPayments = new ArrayList<>();

        List<Payment> foundPaymentsByStudent = paymentRepository.findByAcademy_IdAndPaymentKeyIsNotNullAndStudentOrderByCreatedAtDesc(academyId, foundStudent);

        for (Payment payment : foundPaymentsByStudent) {
            // 결제 완료된거만 추가할 것
            Enrollment foundEnrollment = enrollmentRepository.findByLecture_IdAndStudent_Id(payment.getLecture().getId(), payment.getStudent().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

            if (foundEnrollment.getPaymentYN() == true) {

                CompletePaymentResponse completePayment = new CompletePaymentResponse(payment);

                discountRepository.findById(payment.getDiscountId()).ifPresent(
                        discount -> {
                            completePayment.setDiscountName(discount.getDiscountName());
                        }
                );
                Optional<CancelPayment> foundCancelPayment = cancelPaymentRepository.findByPayment(payment);
                foundCancelPayment.ifPresent(cancelPayment -> completePayment.setDeletedAt(cancelPayment));

                foundPayments.add(completePayment);
            }
        }


        return new PageImpl<>(foundPayments);
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
