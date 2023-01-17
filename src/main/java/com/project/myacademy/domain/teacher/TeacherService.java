package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.teacher.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.project.myacademy.domain.employee.EmployeeRole.ROLE_USER;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TeacherService {

    /**
     * 학원 로그인, 회원 로그인 개념은 아직 추가하지 않았음
     * 학원은 Authentication에 저장되어 있는 필드로, 회원은 회원 이름 필드로 가져올 예정
     * 아래 메서드 모두 추가해야 함
     */
    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final EmployeeRepository employeeRepository;

    /**
     *
     * @param academyId 강사 생성을 진행하는 유저의 학원 id
     * @param employeeId 강사 생성을 진행하는 유저의 id
     * @param request 강사이름, 과목이 들어간 요청 DTO
     */
    public CreateTeacherResponse createTeacher(Long academyId, Long employeeId, CreateTeacherRequest request) {
//        // 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        // 직원의 존재 유무 확인
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        //직원의 role이 강사일때만 강좌에 강사 배정, 아닐 시 에러 처리
        Teacher teacher;
        if(!employee.getEmployeeRole().equals(ROLE_USER)) {
            log.info("강사가 아닙니다");
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        } else {
            teacher = Teacher.addTeacherToLecture(request, employee);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        return CreateTeacherResponse.of(savedTeacher);
    }

    /**
     *
     * @param academyId 강사 정보 변경을 진행하는 유저의 학원 id
     * @param teacherId 강사 정보 변경을 진행하는 유저의 id
     * @param request 변경하고자 하는 정보가 담긴 강사이름, 과목이 들어간 요청 DTO
     */
    public UpdateTeacherResponse updateTeacher(Long academyId, Long teacherId, UpdateTeacherRequest request) {
        // 유효성 검증
        Teacher teacher = validateTeacher(academyId, teacherId);

        // 강사 정보 수정
        teacher.updateTeacherInLecture(request);
        return UpdateTeacherResponse.of(teacherId);
    }

    /**
     *
     * @param academyId 강사 정보 삭제를 진행하는 유저의 학원 id
     * @param teacherId 강사 정보 삭제를를 진행하 유저의 id
     */
    public DeleteTeacherResponse deleteTeacher(Long academyId, Long teacherId) {
        // 유효성 검증
        Teacher teacher = validateTeacher(academyId, teacherId);

        teacherRepository.delete(teacher);
        return DeleteTeacherResponse.of(teacherId);
    }

    private Teacher validateTeacher(Long academyId, Long teacherId) {
//        // 학원의 존재 유무 확인
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        // 강사의 존재 유뮤 확인
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        return teacher;
    }
}
