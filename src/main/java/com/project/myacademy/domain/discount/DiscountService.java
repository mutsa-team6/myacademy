package com.project.myacademy.domain.discount;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.discount.dto.*;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.Enrollment;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DiscountService {

    private final AcademyRepository academyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DiscountRepository discountRepository;

    /**
     * 사용가능한 할인 정책인지 확인
     * (할인정책이 등록되어있어야함)
     *
     * @param academyId 학원 Id
     * @param request 할인정책 Id와 수강 Id 가 담긴 request
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public CheckDiscountResponse checkDiscount(Long academyId, CheckDiscountRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 적용하려는 할인 정책 조회 - 없을시 DISCOUNT_NOT_FOUND 에러발생
        Discount discount = discountRepository.findByDiscountNameAndAcademy(request.getDiscountName(), academy)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));

        // 수강 Id로 수강 조회 - 없을시 ENROLLMENT_NOT_FOUND 에러발생
        Enrollment enrollment = validateEnrollment(request.getEnrollmentId());

        // 요청 DTO에 해당하는 수강 내역이 이미 결제된 수강 이력인지 확인 - 이미 결제되었으면 ALREADY_PAYMENT 에러발생
        if (enrollment.getPaymentYN().equals(true)) {
            throw new AppException(ErrorCode.ALREADY_PAYMENT);
        }

        enrollment.updateDiscountId(discount.getId());
        return CheckDiscountResponse.of(request);
    }

    /**
     * 할인 정책 등록
     *
     * @param academyId 학원 Id
     * @param request 할인률과 할인정책 이름이 담긴 request
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public CreateDiscountResponse createDiscount(Long academyId, CreateDiscountRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 할인 정책이름으로 등록되어있는지 확인함 - 있으면 DUPLICATED_DISCOUNT 에러 발생
        discountRepository.findByDiscountNameAndAcademy(request.getDiscountName(), academy)
                .ifPresent((discount -> {
                    throw new AppException(ErrorCode.DUPLICATED_DISCOUNT);
                }));

        Discount savedDiscount = discountRepository.save(Discount.makeDiscount(request, academy));
        return CreateDiscountResponse.of(savedDiscount);
    }

    /**
     * 할인 정책 전체 조회
     *
     * @param academyId 학원 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public Page<GetDiscountResponse> getAllDiscounts(Long academyId, String account, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);

        //해당 학원에 존재하는 정책들 가져옴
        Page<Discount> discounts = discountRepository.findAllByAcademyAndDeletedAtIsNull(academy, pageable);

        return discounts.map(GetDiscountResponse::of);
    }

    /**
     * 수강 이력에 적용된 할인 정책 조회
     *
     * @param academyId 학원 Id
     * @param enrollmentId 수강이력 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public GetAppliedDiscountResponse getAppliedDiscount(Long academyId, Long enrollmentId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);
        // 수강 Id로 수강 조회 - 없을시 ENROLLMENT_NOT_FOUND 에러발생
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 수강 이력에서 할인 정책 id 찾아오기
        Long discountId = enrollment.getDiscountId();

        // 해당 할인 정책 Id로 할인정책 조회 - 없을시 DISCOUNT_NOT_FOUND 에러발생
        Discount discount = validateDiscountById(discountId);

        return GetAppliedDiscountResponse.of(discount);
    }

    /**
     * 할인정책 삭제
     *
     * @param academyId 학원 Id
     * @param discountId 할인정책 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public DeleteDiscountResponse deleteDiscount(Long academyId, Long discountId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 할인 정책 Id로 할인정책 조회 - 없을시 DISCOUNT_NOT_FOUND 에러발생
        Discount discount = validateDiscountById(discountId);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        discountRepository.delete(discount);
        return DeleteDiscountResponse.of(discountId);
    }


    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        Academy validatedAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validatedAcademy;
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
        return employee;
    }

    // 수강 Id로 수강 조회 - 없을시 ENROLLMENT_NOT_FOUND 에러발생
    private Enrollment validateEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }

    // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
    public void validateAuthorityUser(Employee employee) {
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    // 해당 할인 정책 Id로 할인정책 조회 - 없을시 DISCOUNT_NOT_FOUND 에러발생
    public Discount validateDiscountById(Long discountId) {
        return discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
    }
}
