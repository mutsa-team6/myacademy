package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.enrollment.dto.FindStudentInfoFromEnrollmentByLectureResponse;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.DeleteWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WaitinglistService {

    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;

    private final EnrollmentRepository enrollmentRepository;
    private final WaitinglistRepository waitinglistRepository;
    private final EmailUtil emailUtil;

    /**
     * 수강 대기 전체 조회
     *
     * @param academyId 학원 Id
     * @param account   요청하는 직원 계정
     */
    public Page<ReadAllWaitinglistResponse> readAllWaitinglists(Long academyId, String account, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);

        Page<Waitinglist> waitinglists = waitinglistRepository.findAll(pageable);

        return waitinglists.map(ReadAllWaitinglistResponse::of);
    }

    /**
     * 수강 대기 등록
     *
     * @param academyId 학원 Id
     * @param studentId 학생 Id
     * @param lectureId 강좌 Id
     * @param account   요청하는 직원 계정
     * @return
     */
    public CreateWaitinglistResponse createWaitinglist(Long academyId, Long studentId, Long lectureId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생을 조회 - 없을시 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(studentId);
        // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
        Lecture lecture = validateLectureById(lectureId);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 현재 수강 등록 인원이 최대 수강 정원보다 적으면 대기 등록이 아니라 수강 등록으로 진행해야 함
        if (lecture.getCurrentEnrollmentNumber() < lecture.getMaximumCapacity()) {
            throw new AppException(ErrorCode.CANNOT_REGISTER_WAITINGLIST);
        }

        // 이미 수강 등록되어 있는지 확인
        enrollmentRepository.findByStudentAndLecture(student, lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 대기번호 중복 확인
        waitinglistRepository.findByStudentAndLecture(student, lecture)
                .ifPresent((waitinglist -> {
                    throw new AppException(ErrorCode.DUPLICATED_WAITINGLIST);
                }));

        // 대기번호 저장
        Waitinglist savedWaitinglist = waitinglistRepository.saveAndFlush(Waitinglist.makeWaitinglist(lecture, student));

        // 학생의 이메일로 메시지 전송
        String email = student.getEmail();
        String subject = String.format("MyAcademy 대기 신청 완료 안내 메일");
        String body = String.format("%s님의 %s 대기 신청이 정상적으로 완료되었습니다.%n%n감사합니다.", student.getName(), lecture.getName());
        emailUtil.sendEmail(email, subject, body);

        return CreateWaitinglistResponse.of(savedWaitinglist.getId());
    }

    /**
     * 수강 대기 삭제
     *
     * @param academyId     학원 Id
     * @param studentId     학생 Id
     * @param lectureId     강좌 Id
     * @param waitinglistId 수강 대기 Id
     * @param account       요청하는 직원 계정
     */
    public DeleteWaitinglistResponse deleteWaitinglist(Long academyId, Long studentId, Long lectureId, Long waitinglistId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생을 조회 - 없을시 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(studentId);
        // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
        Lecture lecture = validateLectureById(lectureId);
        // 수강대기 Id로 수강대기를 조회 - 없을시 WAITINGLIST_NOT_FOUND 에러발생
        Waitinglist waitinglist = validateWaitinglistById(waitinglistId);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 대기번호 삭제
        waitinglistRepository.delete(waitinglist);

        // 학생의 이메일로 메시지 전송
        String email = student.getEmail();
        String subject = String.format("MyAcademy 대기 신청 취소 안내 메일");
        String body = String.format("%s님의 %s 대기 신청 취소가 정상적으로 처리되었습니다.%n%n감사합니다.", student.getName(), lecture.getName());
        emailUtil.sendEmail(email, subject, body);

        return DeleteWaitinglistResponse.of(waitinglistId);
    }

    /**
     * UI용 메서드
     * 특정 강좌의 수강대기 학생목록 조회
     *
     * @param academyId      학원 Id
     * @param lectureId      강좌 Id
     * @param requestAccount 요청하는 직원 계정
     */
    public List<FindStudentInfoFromEnrollmentByLectureResponse> findWaitingStudentByLecture(Long academyId, Long lectureId, String requestAccount) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(requestAccount, academy);
        // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
        Lecture foundLecture = validateLectureById(lectureId);

        List<FindStudentInfoFromEnrollmentByLectureResponse> waitingStudents = waitinglistRepository.findByLectureOrderByCreatedAtAsc(foundLecture)
                .stream().map(waitinglist -> new FindStudentInfoFromEnrollmentByLectureResponse(waitinglist.getStudent(), waitinglist.getId(), waitinglist.getLecture().getId())).collect(Collectors.toList());
        Long num = 1L;
        for (FindStudentInfoFromEnrollmentByLectureResponse waitingStudent : waitingStudents) {
            waitingStudent.setWaitingNum(num++);
        }

        return waitingStudents;

    }

    /**
     * UI용 메서드
     * 강좌 수강대기 수 조회
     *
     * @param academyId      학원 Id
     * @param lectureId      강좌 Id
     * @param requestAccount 요청하는 직원 계정
     */
    public Long countWaitingListByLecture(Long academyId, Long lectureId, String requestAccount) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(requestAccount, academy);
        // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
        Lecture lecture = validateLectureById(lectureId);

        return waitinglistRepository.countWaitinglistByLecture(lecture);
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

    // 학생 Id로 학생을 조회 - 없을시 STUDENT_NOT_FOUND 에러발생
    private Student validateStudentById(Long studentId) {
        Student validatedStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return validatedStudent;
    }

    // 강좌 Id로 강좌를 조회 - 없을시 LECTURE_NOT_FOUND 에러발생
    private Lecture validateLectureById(Long lectureId) {
        Lecture validatedLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        return validatedLecture;
    }

    // 수강대기 Id로 수강대기 조회 - 없을시 WAITINGLIST_NOT_FOUND 에러발생
    private Waitinglist validateWaitinglistById(Long waitinglistId) {
        Waitinglist validatedWaitinglist = waitinglistRepository.findById(waitinglistId)
                .orElseThrow(() -> new AppException(ErrorCode.WAITINGLIST_NOT_FOUND));
        return validatedWaitinglist;
    }

    // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
    public void validateAuthorityUser(Employee employee) {
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }
}