package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.academy.repository.AcademyRepository;
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

    /**
     * 학원 로그인, 직원 로그인 개념은 현재는 주석 처리
     * 학원은 학원 이름 혹은 사업자번호로, 직원은 권한 혹은 이름으로 모두 Authentication에서 가져올 예정
     * 등록, 수정, 삭제 작업은 employee 권한이 STAFF, ADMIN(원장)만 가능 -> 해당 코드도 현재는 주석 처리
     * 추후에 중복 코드 메서드로 분리 예정
     */

    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final TeacherRepository teacherRepository;
    private final LectureRepository lectureRepository;

    /**
     *
     * @param academyId
     * @param pageable
     */
    public Page<ReadAllLectureResponse> readAllLectures(Long academyId, Pageable pageable) {
//        // 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        Page<Lecture> lectures = lectureRepository.findAll(pageable);
        return lectures.map(ReadAllLectureResponse::of);
    }

    /**
     * @param academyId 강좌 생성을 직원의 학원 id
     * @param teacherId 강좌에 들어갈 강사의 id
     * @param request   요청 DTO
     */
    public CreateLectureResponse createLecture(Long academyId, Long teacherId, CreateLectureRequest request) {

        // Authentication으로 넘어온 학원 정보 확인, 없으면 강좌 생성 불가
//        academyRepository.findBy학원사업자번호이름(사업자번호)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // Authentication으로 넘어온 직원 정보 확인, 없으면 강좌 생성 불가
//        Employee employee = employeeRepository.findByName(employeeName)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

//        // 강좌가 등록되는 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        // 강사의 존재 유무 확인 -> 강사 null 허용이면 에러처리 안하나...?
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        // 강좌 중복 검사
        lectureRepository.findByName(request.getLectureName())
                .ifPresent((lecture -> {
                    throw new AppException(ErrorCode.DUPLICATED_LECTURE);
                }));

        // 강좌를 개설할 권한이 있는지 확인(강사만 불가능)
//        if(employee.getEmployeeRole.equals((ROLE_USER))) {
//            throw new AppException(ErrorCode.INVALID_PERMISSION);
//        }

        Lecture lecture = Lecture.addLecture(request, teacher);
        Lecture savedLecture = lectureRepository.save(lecture);
        return CreateLectureResponse.of(savedLecture);
    }

    /**
     * @param academyId 강좌 수정 진핼할 직원의 학원 id
     * @param lectureId 수정될 강좌 id
     * @param request   요청 DTO
     */
    public UpdateLectureResponse updateLecture(Long academyId, Long lectureId, UpdateLectureRequest request) {
        // Authentication으로 넘어온 학원 정보 확인, 없으면 강좌 생성 불가
//        academyRepository.findBy학원사업자번호이름(사업자번호)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // Authentication으로 넘어온 직원 정보 확인, 없으면 강좌 생성 불가
//        Employee employee = employeeRepository.findByName(employeeName)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

//        // 강좌가 등록되는 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        // 수정할 강좌 존재 유무 확인
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        lecture.updateLecture(request);

        return UpdateLectureResponse.of(lectureId);
    }

    /**
     * @param academyId 강좌 삭제 진핼할 직원의 학원 id
     * @param lectureId 삭제제 강좌 id
     */
    public DeleteLectureResponse deleteLecture(Long academyId, Long lectureId) {
        // Authentication으로 넘어온 학원 정보 확인, 없으면 강좌 생성 불가
//        academyRepository.findBy학원사업자번호이름(사업자번호)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

        // Authentication으로 넘어온 직원 정보 확인, 없으면 강좌 생성 불가
//        Employee employee = employeeRepository.findByName(employeeName)
//                .orElseThrow(() -> new AppException(ErrorCode.INVALID_PERMISSION));

//        // 강좌가 등록되는 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        // 삭제할 강좌 존재 유무 확인
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new AppException(ErrorCode.LECTURE_NOT_FOUND));

        lectureRepository.delete(lecture);

        return DeleteLectureResponse.of(lectureId);
    }
}