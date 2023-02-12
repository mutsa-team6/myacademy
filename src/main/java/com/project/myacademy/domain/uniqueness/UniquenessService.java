package com.project.myacademy.domain.uniqueness;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.domain.uniqueness.dto.*;
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
@Slf4j
@Transactional(readOnly = true)
public class UniquenessService {

    private final UniquenessRepository uniquenessRepository;
    private final StudentRepository studentRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademyRepository academyRepository;

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param request   특이사항의 요청시 받는 request Dto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateUniquenessResponse createUniqueness(Long academyId, Long studentId, CreateUniquenessRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생이 존재하는지 확인 - 있으면 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(studentId);

        Uniqueness savedUniqueness = uniquenessRepository.save(Uniqueness.toUniqueness(request, student, employee.getName()));

        return CreateUniquenessResponse.of(savedUniqueness);
    }

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param pageable  20개씩 id순서대로(최신순대로)
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public Page<ReadAllUniquenessResponse> readAllUniqueness(Long academyId, Long studentId, Pageable pageable, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생이 존재하는지 확인 - 있으면 STUDENT_NOT_FOUND 에러발생
        Student student = validateStudentById(studentId);

        return uniquenessRepository.findAllByStudent(student, pageable).map(uniqueness -> ReadAllUniquenessResponse.of(uniqueness));
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 수정하려고하는 특이사항 Id
     * @param request      수정내용이 담긴 dto
     * @param account      jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateUniquenessResponse updateUniqueness(Long academyId, Long studentId, Long uniquenessId, UpdateUniquenessRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생이 존재하는지 확인 - 있으면 STUDENT_NOT_FOUND 에러발생
        validateStudentById(studentId);
        // 특이사항 Id로 특이사항 조회 - 없을시 UNIQUENESS_NOT_FOUND 에러발생
        Uniqueness uniqueness = validateUniquenessById(uniquenessId);

        uniqueness.updateUniqueness(request.getBody());
        return UpdateUniquenessResponse.of(uniqueness);
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 삭제하려고 하는 특이사항 Id
     * @param account      jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteUniquenessResponse deleteUniqueness(Long academyId, Long studentId, Long uniquenessId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);
        // 학생 Id로 학생이 존재하는지 확인 - 있으면 STUDENT_NOT_FOUND 에러발생
        validateStudentById(studentId);
        // 특이사항 Id로 특이사항 조회 - 없을시 UNIQUENESS_NOT_FOUND 에러발생
        Uniqueness uniqueness = validateUniquenessById(uniquenessId);

        uniquenessRepository.delete(uniqueness);

        return DeleteUniquenessResponse.of(uniqueness);
    }

    // 특이사항 Id로 특이사항 조회 - 없을시 UNIQUENESS_NOT_FOUND 에러발생
    private Uniqueness validateUniquenessById(Long uniquenessId) {
        return uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));
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

    // 학생 Id로 학생이 존재하는지 확인 - 있으면 STUDENT_NOT_FOUND 에러발생
    private Student validateStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
    }
}
