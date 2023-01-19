package com.project.myacademy.domain.studentlecture;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.payment.Payment;
import com.project.myacademy.domain.payment.PaymentRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.studentlecture.dto.*;
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
public class StudentLectureService {

    private final StudentLectureRepository studentLectureRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;
    private final PaymentRepository paymentRepository;

    /**
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param paymentId 결제 id
     * @param request   등록 요청 DTO
     * @param account   직원 계정
     */
    public CreateStudentLectureResponse createStudentLecture(Long studentId, Long lectureId, Long paymentId,
                                                             CreateStudentLectureRequest request, String account) {

        // 계정, 학생, 강좌, 결제 존재 유무 확인
        Employee employee = validateEmployee(account);
        Student student = validateStudent(studentId);
        Lecture lecture = validateLecture(lectureId);
        Payment payment = validatePayment(paymentId);

        // 직원이 학생-수강을 개설할 권한이 있는지 확인(강사만 불가능)
        if (Employee.hasAuthorityToCreateLecture(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 학생-수강 등록
        StudentLecture savedStudentLecture = studentLectureRepository.save(StudentLecture.createStudentLecture(student, lecture, payment, request));

        return CreateStudentLectureResponse.of(savedStudentLecture.getId());
    }

    /**
     * @param account   직원 계정
     */
//    @Transactional(readOnly = true)
//    public Page<ReadAllStudentLectureResponse> readAllStudentLectures(String account, Pageable pageable) {
//
//        // 계정 인증
//        validateEmployee(account);
//
//        Page<StudentLecture> studentLectures = studentLectureRepository.findAll(pageable);
//
//        return studentLectures.map(ReadAllStudentLectureResponse::of);
//    }
    /**
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param paymentId 결제 id
     * @param studentlectureId 학생-수강 id
     * @param request   수정 요청 DTO
     * @param account   직원 계정
     */
    public UpdateStudentLectureResponse updateStudentLecture(Long studentId, Long lectureId, Long paymentId, Long studentlectureId, UpdateStudentLectureRequest request, String account) {

        // 계정, 학생, 강좌, 결제, 학생-수강 이력 존재 유무 확인
        validateEmployee(account);
        validateStudent(studentId);
        validateLecture(lectureId);
        validatePayment(paymentId);
        StudentLecture studentLecture = validateStudentLecture(studentlectureId);

        // 학생-수강 이력 정보 수정 - 모든 권한 가능
        studentLecture.updateStudentLecture(request);

        return UpdateStudentLectureResponse.of(studentlectureId);
    }
    /**
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param paymentId 결제 id
     * @param studentlectureId 학생-수강 id
     * @param account   직원 계정
     */
    public DeleteStudentLectureResponse deleteStudentLecture(Long studentId, Long lectureId, Long paymentId, Long studentlectureId, String account) {
        // 계정, 학생, 강좌, 결제, 학생-수강 이력 존재 유무 확인
        validateEmployee(account);
        validateStudent(studentId);
        validateLecture(lectureId);
        validatePayment(paymentId);
        StudentLecture studentLecture = validateStudentLecture(studentlectureId);

        // 학생-수강 이력 삭제 - 강사는 불가능하게 하려면 위에 권한 체크해야함
        studentLectureRepository.delete(studentLecture);

        return DeleteStudentLectureResponse.of(studentlectureId);
    }

    private Employee validateEmployee(String account) {
        Employee validatedEmployee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));
        return validatedEmployee;
    }

    private Student validateStudent(Long studentId) {
        Student validatedStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return validatedStudent;
    }

    private Lecture validateLecture(Long lectureId) {
        Lecture validatedLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        return validatedLecture;
    }

    private Payment validatePayment(Long paymentId) {
        Payment validatedPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
        return validatedPayment;
    }

    private StudentLecture validateStudentLecture(Long studentLectureId) {
        StudentLecture validatedStudentLecture = studentLectureRepository.findById(studentLectureId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_LECTURE_NOT_FOUND));
        return validatedStudentLecture;
    }

}
