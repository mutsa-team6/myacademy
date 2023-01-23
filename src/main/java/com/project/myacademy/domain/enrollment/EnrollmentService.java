package com.project.myacademy.domain.enrollment;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.enrollment.dto.*;
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
public class EnrollmentService {

    private final AcademyRepository academyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;

    /**
     * @param academyId 직원의 소속 학원 id
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param request   수강 등록 요청 DTO
     * @param account   직원 계정
     */
    public CreateEnrollmentResponse createEnrollment(Long academyId, Long studentId, Long lectureId, CreateEnrollmentRequest request, String account) {

        // 등록 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌 존재 유무 확인
        Student student = validateStudent(studentId);
        Lecture lecture = validateLecture(lectureId);

        // 직원이 수강을 개설할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 수강 이력 중복 확인
        enrollmentRepository.findByStudentAndLecture(student,lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 수강 내역 생성
        Enrollment enrollment = Enrollment.createEnrollment(student, lecture, employee, request);

        // 수강 내역 저장
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        return CreateEnrollmentResponse.of(savedEnrollment.getId());
    }

    /**
     * @param academyId 직원의 소속 학원 id
     * @param account 직원 계정
     */
    @Transactional(readOnly = true)
    public Page<ReadAllEnrollmentResponse> readAllEnrollments(Long academyId, String account, Pageable pageable) {

        // 조회 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        validateAcademyEmployee(account, academy);

        Page<Enrollment> enrollments = enrollmentRepository.findAll(pageable);

        return enrollments.map(ReadAllEnrollmentResponse::of);
    }

    /**
     * @param academyId     직원의 소속 학원 id
     * @param studentId     학생 id
     * @param lectureId     강좌 id
     * @param enrollmentId  수강 id
     * @param request       수정 요청 DTO
     * @param account       직원 계정
     */
    public UpdateEnrollmentResponse updateEnrollment(Long academyId, Long studentId, Long lectureId, Long enrollmentId, UpdateEnrollmentRequest request, String account) {

        // 수정 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌, 수강 존재 유무 확인
        validateStudent(studentId);
        validateLecture(lectureId);
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 수강 이력 정보 수정(강사는 불가능)
        enrollment.updateEnrollment(employee, request);

        return UpdateEnrollmentResponse.of(enrollmentId);
    }

    /**
     * @param academyId     직원의 소속 학원 id
     * @param studentId     학생 id
     * @param lectureId     강좌 id
     * @param enrollmentId  수강 id
     * @param account       직원 계정
     */
    public DeleteEnrollmentResponse deleteEnrollment(Long academyId, Long studentId, Long lectureId, Long enrollmentId, String account) {

        // 수정 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌, 수강 존재 유무 확인
        validateStudent(studentId);
        validateLecture(lectureId);
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 마지막 수정 직원 필드 수강내역 삭제 직원으로 업데이트 즉시 DB 반영
        enrollment.recordDeleteEmployee(employee);
        enrollmentRepository.saveAndFlush(enrollment);

        // 수강 이력 삭제(강사는 불가능)
        enrollmentRepository.delete(enrollment);
        return DeleteEnrollmentResponse.of(enrollmentId);
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

    private Enrollment validateEnrollment(Long enrollmentId) {
        Enrollment validatedEnrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_LECTURE_NOT_FOUND));
        return validatedEnrollment;
    }

}
