package com.project.myacademy.domain.uniqueness;

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

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param request   특이사항의 요청시 받는 request Dto
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateUniquenessResponse createUniqueness(Long studentId, CreateUniquenessRequest request, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //studentId에 등록된 학생이 있는지 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        Uniqueness savedUniqueness = uniquenessRepository.save(Uniqueness.toUniqueness(request, student));

        return CreateUniquenessResponse.of(savedUniqueness);
    }

    /**
     * @param studentId 특이사항의 대상이 되는 학생 Id
     * @param pageable  20개씩 id순서대로(최신순대로)
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public Page<ReadAllUniquenessResponse> readAllUniqueness(Long studentId, PageRequest pageable, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //studentId에 등록된 학생이 있는지 확인
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        return uniquenessRepository.findAllByStudent(student, pageable).map(uniqueness -> ReadAllUniquenessResponse.of(uniqueness));
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 수정하려고하는 특이사항 Id
     * @param request      수정내용이 담긴 dto
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateUniquenessResponse updateUniqueness(Long studentId, Long uniquenessId, UpdateUniquenessRequest request, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //studentId에 등록된 학생이 있는지 확인
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        //uniquenessId에 등록된 특이사항이 있는지 확인
        Uniqueness uniqueness = uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));

        uniqueness.updateUniqueness(request);

        return UpdateUniquenessResponse.of(uniqueness);
    }

    /**
     * @param studentId    특이사항의 대상이 되는 학생 Id
     * @param uniquenessId 삭제하려고 하는 특이사항 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteUniquenessResponse deleteUniqueness(Long studentId, Long uniquenessId, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //studentId에 등록된 학생이 있는지 확인
        studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.STUDENT_NOT_FOUND));

        //uniquenessId에 등록된 특이사항이 있는지 확인
        Uniqueness uniqueness = uniquenessRepository.findById(uniquenessId)
                .orElseThrow(() -> new AppException(ErrorCode.UNIQUENESS_NOT_FOUND));

        uniquenessRepository.delete(uniqueness);

        return DeleteUniquenessResponse.of(uniqueness);
    }
}
