package com.project.myacademy.domain.waitinglist;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.enrollment.EnrollmentRepository;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.lecture.LectureRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.waitinglist.dto.CreateWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.DeleteWaitinglistResponse;
import com.project.myacademy.domain.waitinglist.dto.ReadAllWaitinglistResponse;
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
public class WaitinglistService {

    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final StudentRepository studentRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WaitinglistRepository waitinglistRepository;


    public Page<ReadAllWaitinglistResponse> readAllWaitinglists(Long academyId, String account, Pageable pageable) {

        // 조회하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        validateAcademyEmployee(account, academy);

        Page<Waitinglist> waitinglists = waitinglistRepository.findAll(pageable);

        return waitinglists.map(ReadAllWaitinglistResponse::of);
    }

    public CreateWaitinglistResponse createWaitinglist(Long academyId, Long studentId, Long lectureId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌 존재 유무 확인
        Student student = validateStudent(studentId);
        Lecture lecture = validateLecture(lectureId);

        // 직원이 대기번호를 등록할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 현재 수강 등록 인원이 최대 수강 정원보다 적으면 대기 등록이 아니라 수강 등록으로 진행해야 함
        if(lecture.getCurrentEnrollmentNumber() < lecture.getMaximumCapacity()) {
            throw new AppException(ErrorCode.CANNOT_REGISTER_WAITINGLIST);
        }

        // 이미 수강 등록되어 있는지 확인
        enrollmentRepository.findByStudentAndLecture(student, lecture)
                .ifPresent((enrollment -> {
                    throw new AppException(ErrorCode.DUPLICATED_ENROLLMENT);
                }));

        // 대기번호 중복 확인
        waitinglistRepository.findByStudentAndLecture(student,lecture)
                .ifPresent((waitinglist -> {
                    throw new AppException(ErrorCode.DUPLICATED_WAITINGLIST);
                }));

        // 대기번호 저장
        Waitinglist savedWaitinglist = waitinglistRepository.saveAndFlush(Waitinglist.makeWaitinglist(lecture, student));
        return CreateWaitinglistResponse.of(savedWaitinglist.getId());
    }

    public DeleteWaitinglistResponse deleteWaitinglist(Long academyId, Long studentId, Long lectureId, Long waitinglistId, String account) {

        // 등록하는 직원 존재 유무 확인(학원 존재 유무, 해당 학원 직원인지 확인)
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 학생, 강좌, 대기번호 존재 유무 확인
        validateStudent(studentId);
        validateLecture(lectureId);
        Waitinglist waitinglist = validateWaitinglist(waitinglistId);

        // 직원이 대기번호를 삭제할 권한이 있는지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 대기번호 삭제
        waitinglistRepository.delete(waitinglist);
        return DeleteWaitinglistResponse.of(waitinglistId);
    }


    public Long countWaitingListByLecture(Long academyId, Long lectureId,String requestAccount) {
        Academy academy = validateAcademy(academyId);

        Employee employee = validateAcademyEmployee(requestAccount, academy);

        Lecture lecture = validateLecture(lectureId);

        return waitinglistRepository.countWaitinglistByLecture(lecture);
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

    private Waitinglist validateWaitinglist(Long waitinglistId) {
        Waitinglist validatedWaitinglist = waitinglistRepository.findById(waitinglistId)
                .orElseThrow(() -> new AppException(ErrorCode.WAITINGLIST_NOT_FOUND));
        return validatedWaitinglist;
    }
}