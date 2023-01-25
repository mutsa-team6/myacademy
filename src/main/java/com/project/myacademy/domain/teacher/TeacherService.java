package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.academy.Academy;
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

    private final AcademyRepository academyRepository;
    private final TeacherRepository teacherRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * @param academyId 학원 id
     * @param employeeId 직원 -> 강사로 등록될 직원의 id
     * @param request   강사이름, 과목이 들어간 등록 요청 DTO
     * @param account   등록 진행하는 직원 계정
     */
    public CreateTeacherResponse createTeacher(Long academyId, Long employeeId, CreateTeacherRequest request, String account) {

        // 학원 존재 유무 확인
        Academy academy = validateAcademy(academyId);

        // 해당 학원 소속 직원인지 확인
        Employee employee = validateAcademyEmployee(account, academy);

        // 현 시점에서 직원 -> 강사 테이블로 등록될 직원이 존재하는지 확인
        Employee employeeToTeacher = validateEmployee(employeeId, academy);

        // 강사 등록하는 주체인 직원의 권한 확인(당연히 강사는 안됨)
        if(Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 일대일 관계이므로 직원이 강사에 2번 등록되면 안됨
        teacherRepository.findByEmployee(employee)
                .ifPresent(teacher -> {
                    throw new AppException(ErrorCode.DUPLICATED_TEACHER);
                });

        // 강사 테이블에 등록될 직원의 권한이 강사일때만 가능
        // 여기서 이미 등록 주체는 직원임
        Teacher teacher;
        if (Teacher.isTeacher(employeeToTeacher)) {
            teacher = Teacher.addTeacher(request, employeeToTeacher);
        } else {
            log.info("강사가 아닙니다");
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        Teacher savedTeacher = teacherRepository.save(teacher);
        return CreateTeacherResponse.of(savedTeacher);
    }

    /**
     * @param academyId 학원 id
     * @param teacherId 수정되고자 하는 강사 id
     * @param request   강사이름, 과목이 들어간 수정 요청 DTO
     * @param account   직원 계정
     */
    public UpdateTeacherResponse updateTeacher(Long academyId, Long teacherId, UpdateTeacherRequest request, String account) {

        // 학원 존재 유무, 강사 존재 유무, 해당 학원에 소속된 직원인지 유효성 검증
        Academy academy = validateAcademy(academyId);
        Teacher teacher = validateTeacher(teacherId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 강사 정보를 수정할 수 있는 권한인지 확인(강사 이상만 가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 강사 정보 수정
        teacher.updateTeacher(request);
        return UpdateTeacherResponse.of(teacherId);
    }

    /**
     * @param academyId 학원 id
     * @param teacherId 수정되고자 하는 강사 id
     * @param account   직원 계정
     */
    public DeleteTeacherResponse deleteTeacher(Long academyId, Long teacherId, String account) {

        // 학원 존재 유무, 강사 존재 유무, 해당 학원에 소속된 직원인지 유효성 검증
        Academy academy = validateAcademy(academyId);
        Teacher teacher = validateTeacher(teacherId);
        Employee employee = validateAcademyEmployee(account, academy);

        // 강사 정보를 삭제할 수 있는 권한인지 확인(강사 이상만 가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 강사 삭제
        teacherRepository.delete(teacher);
        return DeleteTeacherResponse.of(teacherId);
    }

    private Academy validateAcademy(Long academyId) {
        // 학원 존재 유무 확인
        Academy validatedAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validatedAcademy;
    }

    // 직원 ->  강사 테이블로 등록될 직원이 존재하는지 검증
    private Employee validateEmployee(Long employeeId, Academy academy) {
        Employee validateEmployee = employeeRepository.findByIdAndAcademy(employeeId, academy)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return validateEmployee;
    }

    private Teacher validateTeacher(Long teacherId) {
        // 강사의 존재 유뮤 확인
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.TEACHER_NOT_FOUND));
        return teacher;
    }

    private Employee validateAcademyEmployee(String account, Academy academy) {
        // 해당 학원 소속 직원 맞는지 확인
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
        return employee;
    }
}
