package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.lecture.dto.*;
import com.project.myacademy.domain.teacher.Teacher;
import com.project.myacademy.domain.teacher.TeacherRepository;
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
public class LectureService {

    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final TeacherRepository teacherRepository;
    private final LectureRepository lectureRepository;

    /**
     * @param academyId 직원의 소속 학원 id
     * @param account   직원 계정
     * @param pageable
     */
    @Transactional(readOnly = true)
    public Page<ReadAllLectureResponse> readAllLectures(Long academyId, String account, Pageable pageable) {

        // 조회될 학원 존재 유무 확인
        Academy academy = validateAcademy(academyId);

        // 조회 작업을 진행하는 직원이 해당 학원 소속 직원인지 확인
        validateAcademyEmployee(account, academy);

        Page<Lecture> lectures = lectureRepository.findAll(pageable);
        return lectures.map(ReadAllLectureResponse::of);
    }

    /**
     * @param academyId 직원의 소속 학원 id
     * @param teacherId 강좌에 들어갈 강사의 id
     * @param request   요청 DTO
     * @param account   직원 계정
     */
    public CreateLectureResponse createLecture(Long academyId, Long teacherId, CreateLectureRequest request, String account) {

        // 학원 존재 유무 확인
        Academy academy = validateAcademy(academyId);

        // 등록을 진행하는 직원이 해당 학원 소속 직원인지 확인
        Employee employee = validateAcademyEmployee(account, academy);

        // 직원이 강좌를 개설할 권한이 있는지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 강좌에 등록될 강사의 존재 유무 확인
        Teacher teacher = validateTeacher(teacherId);

        // 강좌 중복 확인
        lectureRepository.findByName(request.getLectureName())
                .ifPresent((lecture -> {
                    throw new AppException(ErrorCode.DUPLICATED_LECTURE);
                }));

        Lecture savedLecture = lectureRepository.save(Lecture.addLecture(employee, teacher, request));
        return CreateLectureResponse.of(savedLecture);
    }

    /**
     * @param academyId 직원의 소속 학원 id
     * @param teacherId 수정될 강좌의 강사 id
     * @param lectureId 수정될 강좌 id
     * @param request   요청 DTO
     * @param account   직원 계정
     */
    public UpdateLectureResponse updateLecture(Long academyId, Long teacherId, Long lectureId, UpdateLectureRequest request, String account) {

        // 학원 존재 유무 확인, 수정을 진행하는 직원이 해당 학원 소속 직원인지 확인, 강사 존재 유무, 수정할 강좌 존재 유무 유효성 검증
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);
        validateTeacher(teacherId);
        Lecture lecture = validateLecture(lectureId);

        // 강좌를 수정할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 강좌 정보 수정
        lecture.updateLecture(employee, request);
        return UpdateLectureResponse.of(lectureId);
    }

    /**
     * @param academyId 직원의 소속 학원 id
     * @param teacherId 수정될 강좌에서 강사의 id
     * @param lectureId 삭제 강좌 id
     * @param account   직원 계정
     */
    public DeleteLectureResponse deleteLecture(Long academyId, Long teacherId, Long lectureId, String account) {

        // 학원 존재 유무, 해당 학원의 소속 직원 존재 유무, 강사 존재 유무, 수정할 강좌 존재 유무 유효성 검증
        Academy academy = validateAcademy(academyId);
        Employee employee = validateAcademyEmployee(account, academy);
        validateTeacher(teacherId);
        Lecture lecture = validateLecture(lectureId);

        // 강좌를 수정할 수 있는 권한인지 확인(강사만 불가능)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 마지막 수정 직원 필드 강좌 삭제 직원으로 업데이트 즉시 DB 반영
        lecture.recordDeleteEmployee(employee);
        lectureRepository.saveAndFlush(lecture);

        // 강좌 삭제
        lectureRepository.delete(lecture);
        return DeleteLectureResponse.of(lectureId);
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

    private Teacher validateTeacher(Long teacherId) {
        Teacher validatedTeacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
        return  validatedTeacher;
    }

    private Lecture validateLecture(Long lectureId) {
        Lecture validatedLecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));
        return  validatedLecture;
    }
}