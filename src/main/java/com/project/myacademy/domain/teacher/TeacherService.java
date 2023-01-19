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
     * 회원만 로그인 하는 것으로 변경
     * 회원은 회원 계정 필드로 가져올 예정
     */
    private final TeacherRepository teacherRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * @param request 강사이름, 과목이 들어간 등록 요청 DTO
     * @param account 직원 계정
     */
    public CreateTeacherResponse createTeacher(CreateTeacherRequest request, String account) {

        // Authentication으로 넘어온 직원의 계정 존재 유무 확인
        Employee employee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        //직원의 role이 강사일때만 강좌에 강사 배정, 아닐 시 에러 처리
        Teacher teacher;
        if(Teacher.isNotTeacher(employee)) {
            log.info("강사가 아닙니다");
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        } else {
            teacher = Teacher.addTeacher(request, employee);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        return CreateTeacherResponse.of(savedTeacher);
    }

    /**
     * @param teacherId 수정되고자 하는 강사 id
     * @param request   강사이름, 과목이 들어간 수정 요청 DTO
     * @param account   직원 계정
     */
    public UpdateTeacherResponse updateTeacher(Long teacherId, UpdateTeacherRequest request, String account) {

        // 유효성 검증
        Teacher teacher = validateTeacher(account, teacherId);

        // 강사 정보 수정
        teacher.updateTeacher(request);
        return UpdateTeacherResponse.of(teacherId);
    }

    /**
     * @param teacherId 수정되고자 하는 강사 id
     * @param account   직원 계정
     */
    public DeleteTeacherResponse deleteTeacher(Long teacherId, String account) {

        // 유효성 검증
        Teacher teacher = validateTeacher(account, teacherId);

        teacherRepository.delete(teacher);
        return DeleteTeacherResponse.of(teacherId);
    }

    private Teacher validateTeacher(String account, Long teacherId) {

        // Authentication으로 넘어온 직원의 계정 존재 유무 확인
        Employee employee = employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));

        // 강사의 존재 유뮤 확인
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));

        return teacher;
    }
}
