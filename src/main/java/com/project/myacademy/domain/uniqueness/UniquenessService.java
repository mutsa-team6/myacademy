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
import org.springframework.data.domain.PageRequest;
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
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateUniquenessResponse createUniqueness(Long academyId, Long studentId, CreateUniquenessRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);

        Uniqueness savedUniqueness = uniquenessRepository.save(Uniqueness.toUniqueness(request, student));

        return CreateUniquenessResponse.of(savedUniqueness);
    }

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param pageable  20개씩 id순서대로(최신순대로)
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public Page<ReadAllUniquenessResponse> readAllUniqueness(Long academyId, Long studentId, PageRequest pageable, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);

        return uniquenessRepository.findAllByStudent(student, pageable).map(uniqueness -> ReadAllUniquenessResponse.of(uniqueness));
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 수정하려고하는 특이사항 Id
     * @param request      수정내용이 담긴 dto
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateUniquenessResponse updateUniqueness(Long academyId, Long studentId, Long uniquenessId, UpdateUniquenessRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);
        //uniquenessId에 등록된 특이사항이 있는지 확인
        Uniqueness uniqueness = validateUniqueness(uniquenessId);

        uniqueness.updateUniqueness(request);

        return UpdateUniquenessResponse.of(uniqueness);
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 삭제하려고 하는 특이사항 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteUniquenessResponse deleteUniqueness(Long academyId, Long studentId, Long uniquenessId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //student Id에 해당하는 학생이 존재하는지 확인
        Student student = validateStudent(studentId);
        //uniquenessId에 등록된 특이사항이 있는지 확인
        Uniqueness uniqueness = validateUniqueness(uniquenessId);

        uniquenessRepository.delete(uniqueness);

        return DeleteUniquenessResponse.of(uniqueness);
    }

    private Uniqueness validateUniqueness(Long uniquenessId) {
        //학생 특이사항 존재 유무 확인
        Uniqueness validateUniqueness = uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));
        return validateUniqueness;
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
        // 학생 존재 유무 확인
        Student validateStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));
        return validateStudent;
    }
}
