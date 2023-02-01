package com.project.myacademy.domain.discount;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.discount.dto.*;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
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
    private final LectureRepository lectureRepository;
    private final StudentRepository studentRepository;
    private final DiscountRepository discountRepository;

    /**
     * 이미 할인정책은 등록되어 있고, 할인정책 적용 가능한지 확인
     *
     * @param academyId
     * @param request
     * @param account
     */
    public CheckDiscountResponse checkDiscount(Long academyId, CheckDiscountRequest request, String account) {

        // 작업 진행하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 작업을 수행할 권한이 있는지 확인 (강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 적용 요청한 할인 정책 존재 유무 확인
        Discount discount = discountRepository.findByDiscountNameAndAcademy(request.getDiscountName(), academy)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));

        // 요청 DTO의 수강 id로 수강이력 존재 유무 확인
        Enrollment enrollment = validateEnrollment(request.getEnrollmentId());

        // 요청 DTO의 수강이력의 학생 id로 학생, 강좌 존재 유무 확인
//        validateStudent(enrollment.getStudent().getId());
//        validateLecture(enrollment.getLecture().getId());

        // 요청 DTO에 해당하는 수강 내역이 이미 결제된 수강 이력인지 확인
        if (enrollment.getPaymentYN().equals(true)) {
            throw new AppException(ErrorCode.ALREADY_PAYMENT);
        }

        enrollment.updateDiscountId(discount.getId());
        return CheckDiscountResponse.of(request);
    }

    public CreateDiscountResponse createDiscount(Long academyId, CreateDiscountRequest request, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 할인 정책을 등록할 권한이 있는지 확인 (강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 할인 정책 중복 확인
        discountRepository.findByDiscountNameAndAcademy(request.getDiscountName(), academy)
                .ifPresent((discount -> {
                    throw new AppException(ErrorCode.DUPLICATED_DISCOUNT);
                }));

        Discount savedDiscount = discountRepository.save(Discount.makeDiscount(request, academy));
        return CreateDiscountResponse.of(savedDiscount);
    }

    public Page<GetDiscountResponse> getAllDiscounts(Long academyId, String account, Pageable pageable) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        validateAcademyEmployee(account, academy);

        //해당 학원에 존재하는 정책들 가져옴
        Page<Discount> discounts = discountRepository.findAllByAcademy(academy, pageable);

        return discounts.map(GetDiscountResponse::of);
    }

    public GetAppliedDiscountResponse getAppliedDiscount(Long academyId, Long enrollmentId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        validateAcademyEmployee(account, academy);

        // 할인이 적용될 수강 이력 존재 유무 확인
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 수강 이력에서 할인 정책 id 찾아오기
        Long discountId = enrollment.getDiscountId();

        // 해당 할인 정책 id가 유효한 할인 정책인지 확인
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));

        return GetAppliedDiscountResponse.of(discount);
    }

    public DeleteDiscountResponse deleteDiscount(Long academyId, Long discountId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));

        // 할인 정책을 등록할 권한이 있는지 확인 (강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        discountRepository.delete(discount);
        return DeleteDiscountResponse.of(discountId);
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

//    private Student validateStudent(Long studentId) {
//        Student validatedStudent = studentRepository.findById(studentId)
//                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
//        return validatedStudent;
//    }
//
//    private Lecture validateLecture(Long lectureId) {
//        Lecture validatedLecture = lectureRepository.findById(lectureId)
//                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
//        return validatedLecture;
//    }

    private Enrollment validateEnrollment(Long enrollmentId) {
        Enrollment validatedEnrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        return validatedEnrollment;
    }

}
