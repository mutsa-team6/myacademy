package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.lecture.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LectureService {

    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final LectureRepository lectureRepository;

    /**
     * 모든 강좌 조회
     *
     * @param academyId 직원의 소속 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional(readOnly = true)
    public Page<ReadAllLectureResponse> readAllLectures(Long academyId, String account, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);

        Page<Lecture> lectures = lectureRepository.findAll(pageable);

        return lectures.map(ReadAllLectureResponse::of);
    }

    /**
     * 강좌 생성
     *
     * @param academyId  직원의 소속 학원 id
     * @param employeeId 강좌에 들어갈 강사의 id
     * @param request    등록 요청 DTO
     * @param account    jwt로 받아온 사용자(Employee) 계정
     */
    public CreateLectureResponse createLecture(Long academyId, Long employeeId, CreateLectureRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 강좌에 등록될 강사의 존재 유무 확인 - 없으면 TEACHER_NOT_FOUND 에러발생
        Employee teacher = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        // 강사의 권한이 ROLE_USER 인지 확인 - 아니면 INVALID_PERMISSION 에러발생
        if (teacher.getEmployeeRole().equals(EmployeeRole.ROLE_STAFF)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 강좌 중복 확인 - 있으면 DUPLICATED_LECTURE 에러발생
        lectureRepository.findByName(request.getLectureName())
                .ifPresent((lecture -> {
                    throw new AppException(ErrorCode.DUPLICATED_LECTURE);
                }));

        Lecture savedLecture = lectureRepository.save(Lecture.addLecture(employee, teacher, request, academyId));
        return CreateLectureResponse.of(savedLecture);
    }

    /**
     * 강좌 수정
     *
     * @param academyId 직원의 소속 학원 id
     * @param lectureId 수정될 강좌 id
     * @param request   수정 요청 DTO
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public UpdateLectureResponse updateLecture(Long academyId, Long lectureId, UpdateLectureRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생이 해당 학원 소속 직원인지 확인
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 강좌 Id로 강좌 조회 - 없으면 LECTURE_NOT_FOUND 에러발생
        Lecture lecture = validateLecture(lectureId);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 강좌 정보 수정
        lecture.updateLecture(employee, request);

        return UpdateLectureResponse.of(lectureId);
    }

    /**
     * 강좌 삭제
     *
     * @param academyId 직원의 소속 학원 id
     * @param lectureId 삭제 강좌 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public DeleteLectureResponse deleteLecture(Long academyId, Long lectureId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생 유무 확인
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 강좌 Id로 강좌 조회 - 없으면 LECTURE_NOT_FOUND 에러발생
        Lecture lecture = validateLecture(lectureId);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        // 마지막 수정 직원 필드 강좌 삭제 직원으로 업데이트 즉시 DB 반영
        lecture.recordDeleteEmployee(employee);
        lectureRepository.saveAndFlush(lecture);

        // 강좌 삭제
        lectureRepository.delete(lecture);

        return DeleteLectureResponse.of(lectureId);
    }

    /**
     * UI용 메서드
     * 모든 해당 강사의 모든 강좌 조회
     *
     * @param academyId 학원 Id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     * @param teacherId 강사 Id
     */
    @Transactional(readOnly = true)
    public Page<ReadAllLectureResponse> readAllLecturesByTeacherId(Long academyId, String account, Long teacherId, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);
        //직원(강사) Id와 학원으로 직원을 조회 - 없을시 EMPLOYEE_NOT_FOUND 에러발생
        Employee foundTeacher = validateAcademyEmployeeId(teacherId, academy);

        // 해당 강사의 모든 강의를 가져온다.
        Page<Lecture> foundLectures = lectureRepository.findByEmployeeAndFinishDateGreaterThanOrderByStartDate(foundTeacher, LocalDate.now(), pageable);

        return foundLectures.map(ReadAllLectureResponse::of);
    }

    /**
     * UI용 메서드
     * 종료되지 않은 모든 강의를 조회
     *
     * @param academyId 학원 Id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional(readOnly = true)
    public Page<ReadAllLectureResponse> readAllLecturesForEnrollment(Long academyId, String account, Pageable pageable) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);

        // 강의가 종료되지 않은 모든 강의를 최신순으로 가져온다.
        Page<Lecture> foundLectures = lectureRepository.findByAcademyIdAndFinishDateGreaterThanOrderByCreatedAtDesc(academyId, LocalDate.now(), pageable);

        return foundLectures.map(ReadAllLectureResponse::of);
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

    //직원(강사) Id와 학원으로 직원을 조회 - 없을시 EMPLOYEE_NOT_FOUND 에러발생
    private Employee validateAcademyEmployeeId(Long employeeId, Academy academy) {

        Employee validateEmployee = employeeRepository.findByIdAndAcademy(employeeId, academy)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return validateEmployee;
    }

    // 강좌 Id로 강좌 조회 - 없으면 LECTURE_NOT_FOUND 에러발생
    private Lecture validateLecture(Long lectureId) {
        Lecture validatedLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        return validatedLecture;
    }

    // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
    public void validateAuthorityUser(Employee employee) {
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }
}