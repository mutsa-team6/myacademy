package com.project.myacademy.domain.lecture;

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

import static com.project.myacademy.domain.employee.EmployeeRole.ROLE_USER;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LectureService {

    private final EmployeeRepository employeeRepository;
    private final TeacherRepository teacherRepository;
    private final LectureRepository lectureRepository;

    /**
     * @param account   직원 계정
     * @param pageable
     */
    @Transactional(readOnly = true)
    public Page<ReadAllLectureResponse> readAllLectures(String account, Pageable pageable) {
        // 유효한 계정인지 확인
//        validateEmployee(account);

        Page<Lecture> lectures = lectureRepository.findAll(pageable);
        return lectures.map(ReadAllLectureResponse::of);
    }

    /**
     * @param teacherId 강좌에 들어갈 강사의 id
     * @param request   요청 DTO
     * @param account   직원 계정
     */
    public CreateLectureResponse createLecture(Long teacherId, CreateLectureRequest request, String account) {

        // 직원 계정 존재 유무 확인
//        Employee employee = validateEmployee(account);

        // 강사의 존재 유무 확인
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        // 직원이 강좌를 개설할 권한이 있는지 확인(강사만 불가능)
//        if(Employee.hasAuthorityToCreateLecture(employee)) {
//            throw new AppException(ErrorCode.INVALID_PERMISSION);
//        }

        // 강좌 중복 확인
        lectureRepository.findByName(request.getLectureName())
                .ifPresent((lecture -> {
                    throw new AppException(ErrorCode.DUPLICATED_LECTURE);
                }));

        Lecture savedLecture = lectureRepository.save(Lecture.addLecture(request, teacher));
        return CreateLectureResponse.of(savedLecture);
    }

    /**
     * @param teacherId 수정될 강좌에서 강사의 id
     * @param lectureId 수정될 강좌 id
     * @param request   요청 DTO
     * @param account   직원 계정
     */
    public UpdateLectureResponse updateLecture(Long teacherId, Long lectureId, UpdateLectureRequest request, String account) {

        // 직원 계정 존재 유무 확인
//        Employee employee = validateEmployee(account);

        // 강사의 존재 유무 확인
        Teacher teacher = validateTeacher(teacherId);

        // 수정할 강좌 존재 유무 확인
        Lecture lecture = validateLecture(lectureId);

        // 강좌 정보 수정
        lecture.updateLecture(request);

        return UpdateLectureResponse.of(lectureId);
    }

    /**
     * @param teacherId 수정될 강좌에서 강사의 id
     * @param lectureId 삭제 강좌 id
     * @param account   직원 계정
     */
    public DeleteLectureResponse deleteLecture(Long teacherId, Long lectureId, String account) {

        // 직원 계정 존재 유무 확인
//        Employee employee = validateEmployee(account);

        // 강사의 존재 유무 확인
        Teacher teacher = validateTeacher(teacherId);

        // 삭제할 강좌 존재 유무 확인
        Lecture lecture = validateLecture(lectureId);

        // 강좌 삭제
        lectureRepository.delete(lecture);

        return DeleteLectureResponse.of(lectureId);
    }


//    private Employee validateEmployee(String account) {
//        Employee validatedEmployee = employeeRepository.findByAccount(account)
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        return validatedEmployee;
//    }

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