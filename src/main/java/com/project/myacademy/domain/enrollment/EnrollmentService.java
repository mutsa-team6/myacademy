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
import com.project.myacademy.domain.waitinglist.Waitinglist;
import com.project.myacademy.domain.waitinglist.WaitinglistRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final WaitinglistRepository waitinglistRepository;

    /**
     * @param academyId 직원의 소속 학원 id
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param account   직원 계정
     */
    public CreateEnrollmentResponse createEnrollment(Long academyId, Long studentId, Long lectureId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
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
        Enrollment savedEnrollment;

        // 현재까지 강좌에 등록된 인원수 가져오기
        Long currentEnrollmentNumber = enrollmentRepository.countByLecture_Id(lecture.getId());

        // (해당 강좌에 등록할 인원 + 이미 등록되어 있는 인원)이 최대 수강정원을 넘지 않으면 수강 내역 저장
        if(currentEnrollmentNumber + 1 <= lecture.getMaximumCapacity()) {
            lecture.plusCurrentEnrollmentNumber();
            savedEnrollment = enrollmentRepository.save(Enrollment.createEnrollment(student, lecture, employee,academyId));
        }
        // 그렇지 않으면 수강정원 초과 에러처리
        else {
//            waitinglistRepository.saveAndFlush(Waitinglist.makeWaitinglist(lecture, student));
            throw new AppException(ErrorCode.OVER_REGISTRATION_NUMBER);
        }

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

        // 수정 진행하는 직원 유무 존재(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌, 수강 존재 유무 확인
        validateStudent(studentId);
        validateLecture(lectureId);
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 직원이 수강을 수정할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

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
    public DeleteEnrollmentResponse deleteEnrollment(Long academyId, Long studentId, Long lectureId, Long enrollmentId, CreateEnrollmentRequest request, String account) {

        // 삭제 진행하는 직원 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌, 수강 존재 유무 확인
        validateStudent(studentId);
        Lecture lecture = validateLecture(lectureId);
        Enrollment enrollment = validateEnrollment(enrollmentId);

        // 직원이 수강 삭제, 대기번호 -> 수강등록을 진행할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 마지막 수정 직원 필드 -> 수강내역 삭제 직원으로 업데이트
        // 현재 등록 인원수 1명 down
        enrollment.recordDeleteEmployee(employee);
        // DB 즉시 반영
        enrollmentRepository.saveAndFlush(enrollment);

        // 수강 이력 먼저 삭제
        enrollmentRepository.delete(enrollment);
        // 현재 등록인원 -1
        lecture.minusCurrentEnrollmentNumber();

        // 대기번호 존재 유무 확인
        Waitinglist waitinglist = waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(lecture)
                .orElseThrow(() -> new AppException(ErrorCode.WAITINGLIST_NOT_FOUND));

        // 대기번호 -> 수강등록으로 정보 변경
        Long newEnrollmentId = createEnrollmentFromWaitinglist(academy.getId(), waitinglist.getStudent().getId(), waitinglist.getLecture().getId(), request, account);

        // 수강등록이 끝나면 기존 대기번호 삭제
        waitinglistRepository.delete(waitinglist);

        return DeleteEnrollmentResponse.of(enrollmentId, newEnrollmentId);
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
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
        return validatedEnrollment;
    }

    // 대기번호 -> 수강등록으로 이동하게 하는 메서드
    private Long createEnrollmentFromWaitinglist(Long academyId, Long studentId, Long lectureId, CreateEnrollmentRequest request, String account) {

        // 등록 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌 존재 유무 확인
        Student student = validateStudent(studentId);
        Lecture lecture = validateLecture(lectureId);

        // 수강 이력 중복 확인
        enrollmentRepository.findByStudentAndLecture(student,lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 수강 내역 생성
        lecture.plusCurrentEnrollmentNumber();
        Enrollment enrollment = Enrollment.createEnrollment(student, lecture, employee,academyId);

        // 수강 내역 저장 즉시 DB 반영 -> 반환 DTO에 새롭게 생성된 수강 내역의 enrollmentId 추출하기 위함
//        Enrollment newEnrollment = enrollmentRepository.saveAndFlush(enrollment);
        Enrollment newEnrollment = enrollmentRepository.save(enrollment);

        return newEnrollment.getId();
    }

    /**
     * 결제 UI용 메서드
     */

    public Page<FindEnrollmentResponse> findEnrollmentForPay(Long academyId, String studentName) {

        // 학원과 학생 이름으로 student 객체를 찾아온다.
        List<Student> foundStudents = studentRepository.findByAcademyIdAndName(academyId, studentName);

        // 모두 이 컬렉션에 저장할 것이다.
        List<FindEnrollmentResponse> finalEnrollments = new ArrayList<>();

        // student 객체로 수강등록 데이터를 찾아온다 ( 근데 여러개일 수 있다.)
        for (Student foundStudent : foundStudents) {
            List<Enrollment> founds = enrollmentRepository.findByStudentOrderByCreatedAtDesc(foundStudent);
            for (Enrollment found : founds) {
                finalEnrollments.add(new FindEnrollmentResponse(found));
            }
        }
        return new PageImpl<>(finalEnrollments);
    }

    /**
     * UI용 메서드, 해당 학원의 모든 수강신청내역 가져오기
     */
    public Page<FindEnrollmentResponse> findAllEnrollmentForPay(Long academyId,Pageable pageable) {

        //해당 학원의 모든 수강 신청 내역을 page 로 가져온다.
        Page<Enrollment> foundAllEnrollments = enrollmentRepository.findAllByAcademyIdOrderByCreatedAtDesc(academyId,pageable);

        return foundAllEnrollments.map(enrollment -> new FindEnrollmentResponse(enrollment));
    }
}
