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
import com.project.myacademy.domain.waitinglist.WaitinglistRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.domain.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final EmailService emailService;

    /**
     * 수강 이력 등록
     *
     * @param academyId 직원의 소속 학원 id
     * @param studentId 학생 id
     * @param lectureId 강좌 id
     * @param account   직원 계정
     */
    public CreateEnrollmentResponse createEnrollment(Long academyId, Long studentId, Long lectureId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 학생, 강좌 존재 유무 확인
        Student student = validateStudentById(studentId);
        Lecture lecture = validateLectureById(lectureId);

        // 직원이 수강을 개설할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 수강 이력 중복 확인
        enrollmentRepository.findByStudentAndLecture(student, lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 수강 내역 생성
        Enrollment savedEnrollment;

        // 현재까지 강좌에 등록된 인원수 가져오기
        Long currentEnrollmentNumber = enrollmentRepository.countByLecture_Id(lecture.getId());

        // (해당 강좌에 등록할 인원 + 이미 등록되어 있는 인원)이 최대 수강정원을 넘지 않으면 수강 내역 저장
        if (currentEnrollmentNumber + 1 <= lecture.getMaximumCapacity()) {
            lecture.plusCurrentEnrollmentNumber();
            savedEnrollment = enrollmentRepository.save(Enrollment.createEnrollment(student, lecture, employee, academyId));
        }
        // 그렇지 않으면 수강정원 초과 에러처리
        else {
            throw new AppException(ErrorCode.OVER_REGISTRATION_NUMBER);
        }

        // 학생의 이메일로 메시지 전송
//        String email = student.getEmail();
//        String subject = "MyAcademy 수강 등록 안내 메일";
//        String body = String.format("%s님의 %s 수강 등록이 정상적으로 완료되었습니다.%n%n감사합니다.", student.getName(), lecture.getName());
//
//        try {
//            emailService.sendEmail(email, subject, body);
//        } catch (MailException | MessagingException e){
//            log.info("이메일 전송 에러 발생 [{}]", e.getMessage());
//        }

        return CreateEnrollmentResponse.of(savedEnrollment.getId());
    }

    /**
     * 수강이력 전체 조회
     *
     * @param academyId 직원의 소속 학원 id
     * @param account   직원 계정
     */
    @Transactional(readOnly = true)
    public Page<ReadAllEnrollmentResponse> readAllEnrollments(Long academyId, String account, Pageable pageable) {

        // 조회 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        validateRequestEmployeeByAcademy(account, academy);

        Page<Enrollment> enrollments = enrollmentRepository.findAll(pageable);

        return enrollments.map(ReadAllEnrollmentResponse::of);
    }

    /**
     * 수강 이력 수정
     *
     * @param academyId    직원의 소속 학원 id
     * @param studentId    학생 id
     * @param lectureId    강좌 id
     * @param enrollmentId 수강 id
     * @param request      수정 요청 DTO
     * @param account      직원 계정
     */
    public UpdateEnrollmentResponse updateEnrollment(Long academyId, Long studentId, Long lectureId, Long enrollmentId, UpdateEnrollmentRequest request, String account) {

        // 수정 진행하는 직원 유무 존재(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생을 조회 - 없을시 STUDENT_NOT_FOUND 에러발생재 유무 확인
        validateStudentById(studentId);
        validateLectureById(lectureId);
        Enrollment enrollment = validateEnrollmentById(enrollmentId);

        // 직원이 수강을 수정할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 수강 이력 정보 수정
        enrollment.updateEnrollment(employee, request);

        return UpdateEnrollmentResponse.of(enrollmentId);
    }

    /**
     * 수강 이력 삭제
     *
     * @param academyId    직원의 소속 학원 id
     * @param studentId    학생 id
     * @param lectureId    강좌 id
     * @param enrollmentId 수강 id
     * @param account      직원 계정
     */
    public DeleteEnrollmentResponse deleteEnrollment(Long academyId, Long studentId, Long lectureId, Long enrollmentId, String account) {

        // 삭제 진행하는 직원 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 학생, 강좌, 수강 존재 유무 확인
        Student student = validateStudentById(studentId);
        Lecture lecture = validateLectureById(lectureId);
        Enrollment enrollment = validateEnrollmentById(enrollmentId);

        // 직원이 수강 삭제, 대기번호 -> 수강등록을 진행할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 마지막 수정 직원 필드 -> 수강내역 삭제 직원으로 업데이트
        // 현재 등록 인원수 1명 down
        enrollment.recordDeleteEmployee(employee);
        // DB 즉시 반영
        enrollmentRepository.saveAndFlush(enrollment);

        // 다음 대기번호 존재하든 안하든 수강 이력 먼저 삭제
        enrollmentRepository.delete(enrollment);

        // 학생의 이메일로 메시지 전송
//        String email = student.getEmail();
//        String subject = "MyAcademy 수강 취소 안내 메일";
//        String body = String.format("%s님의 %s 수강 취소 신청이 정상적으로 처리되었습니다.%n%n감사합니다.", student.getName(), lecture.getName());
//        try {
//            emailService.sendEmail(email, subject, body);
//        } catch (MailException e2){
//            log.info("이메일 전송 에러 발생 [{}]", e2.getMessage());
//        } catch (MessagingException e) {
//            log.info("이메일 전송 에러 발생 [{}]", e.getMessage());
//        }

        // 현재 등록인원 -1
        lecture.minusCurrentEnrollmentNumber();

        // 다음 대기번호가 존재하면 추가적으로 대기번호 -> 수강등록으로 정보 변경 후 기존 대기번호 삭제
        waitinglistRepository.findTopByLectureOrderByCreatedAtAsc(lecture)
                .ifPresent(waitinglist -> {
                    createEnrollmentFromWaitinglist(academy.getId(), waitinglist.getStudent().getId(), waitinglist.getLecture().getId(), account);
                    waitinglistRepository.delete(waitinglist);
                });

        return DeleteEnrollmentResponse.of(enrollmentId);
    }

    // 대기번호 -> 수강등록으로 이동하게 하는 메서드
    private void createEnrollmentFromWaitinglist(Long academyId, Long studentId, Long lectureId, String account) {

        // 등록 주체 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 학생, 강좌 존재 유무 확인
        Student student = validateStudentById(studentId);
        Lecture lecture = validateLectureById(lectureId);

        // 수강 이력 중복 확인
        enrollmentRepository.findByStudentAndLecture(student, lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 수강 내역 생성
        lecture.plusCurrentEnrollmentNumber();
        Enrollment enrollment = Enrollment.createEnrollment(student, lecture, employee, academyId);

        // 수강 내역 저장
        enrollmentRepository.save(enrollment);
    }

    /**
     * 결제 UI용 메서드
     */
    public Page<FindEnrollmentResponse> findEnrollmentForPay(Long academyId, String studentName, Pageable pageable) {

        // 학원과 학생 이름으로 student 객체를 찾아온다.
        Page<Student> foundStudents = studentRepository.findByAcademyIdAndName(academyId, studentName, pageable);

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
     * 학생 상세 조회 페이지용 UI 메서드
     * 수강신청 내역중에 결제가 완료된 내역만 가져온다.
     */
    public Page<FindEnrollmentResponse> findEnrollmentByStudentId(Long academyId, Long studentId, Pageable pageable) {

        // 학원 존재 유무 확인
        validateAcademyById(academyId);

        // 학생 존재 유무 확인
        Student foundStudent = validateStudentById(studentId);

        return enrollmentRepository.findByStudentAndPaymentYNIsTrue(foundStudent, pageable).map(FindEnrollmentResponse::new);
    }

    /**
     * UI용 메서드, 해당 학원의 모든 수강신청내역 가져오기 ( 최신순으로, 결제 내역이 false인 데이터만)
     */
    public Page<FindEnrollmentResponse> findAllEnrollmentForPay(Long academyId, Pageable pageable) {

        //해당 학원의 모든 수강 신청 내역을 page 로 가져온다.
        Page<Enrollment> foundAllEnrollments = enrollmentRepository.findAllByAcademyIdAndPaymentYNIsFalseOrderByCreatedAtDesc(academyId, pageable);

        return foundAllEnrollments.map(FindEnrollmentResponse::new);
    }

    /**
     * UI용 메서드, 해당 학원의 특정 학생의 수강신청 내역
     * 결제 완료되면 보여주기 위해 존재
     */
    public FindEnrollmentResponse findEnrollmentForPaySuccess(Long studentId, Long lectureId) {

        //해당 학원의 특정 학생의 특정 과목 수강신청 내역을 가져온다.
        Enrollment foundEnrollment = enrollmentRepository.findByLecture_IdAndStudent_Id(lectureId, studentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));

        return new FindEnrollmentResponse(foundEnrollment);
    }

    /**
     * 결제 완료된 수강신청내역을 활용해서 출석부를 표시하기 위해 만든 메서드
     */
    public Page<FindStudentInfoFromEnrollmentByLectureResponse> findStudentInfoFromEnrollmentByLecture(Long academyId, String requestAccount, Long lectureId, Pageable pageable) {

        // 조회 진행하는 직원 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        validateRequestEmployeeByAcademy(requestAccount, academy);

        // 강좌 존재 유무 확인
        Lecture foundLecture = validateLectureById(lectureId);

        return enrollmentRepository.findByLectureAndPaymentYNIsTrue(foundLecture, pageable).map(FindStudentInfoFromEnrollmentByLectureResponse::new);
    }

    /**
     * 결제 완료 여부와 상관없이 수강신청내역을 활용해서 수강 신청자 명단을 위해 만든 메서드 UI용
     */
    public List<FindStudentInfoFromEnrollmentByLectureResponse> findAllStudentInfoFromEnrollmentByLecture(Long academyId, String requestAccount, Long lectureId) {

        // 조회 진행하는 직원 권한 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademyById(academyId);
        validateRequestEmployeeByAcademy(requestAccount, academy);

        // 강좌 존재 유무 확인
        Lecture foundLecture = validateLectureById(lectureId);

        return enrollmentRepository.findByLecture(foundLecture).stream().map(FindStudentInfoFromEnrollmentByLectureResponse::new).collect(Collectors.toList());
    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        return academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        return employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
    }

    // 학생 Id로 학생을 조회 - 없을시 STUDENT_NOT_FOUND 에러발생
    private Student validateStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }

    // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
    private Lecture validateLectureById(Long lectureId) {
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
    }

    // 수강이력 Id로 수강이력을 조회 - 없을시 ENROLLMENT_NOT_FOUND 에러발생
    private Enrollment validateEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new AppException(ErrorCode.ENROLLMENT_NOT_FOUND));
    }
}
