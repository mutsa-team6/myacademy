package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.parent.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ParentService {

    private final ParentRepository parentRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademyRepository academyRepository;

    /**
     * @param academyId 학원 id
     * @param request   부모 정보가 담긴 request
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateParentResponse createParent(Long academyId, CreateParentRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //중복체크
        parentRepository.findByPhoneNum(request.getPhoneNum())
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_PARENT);
                });

        Parent savedParent = parentRepository.save(Parent.toParent(request));

        return CreateParentResponse.of(savedParent);
    }

    /**
     * @param parentId  찾을 부모 Id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public ReadParentResponse readParent(Long academyId, long parentId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        //parentId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        return ReadParentResponse.of(parent);
    }

    /**
     * @param parentId  수정할 부모 Id
     * @param academyId 학원 id
     * @param request   수정할 부모 정보를 담은 requestDto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateParentResponse updateParent(Long academyId, Long parentId, UpdateParentRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //parentId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        parent.updateParent(request);

        return UpdateParentResponse.of(parent);
    }

    /**
     * @param parentId  삭제할 부모 Id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteParentResponse deleteParent(Long academyId, Long parentId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        //parentId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        parentRepository.delete(parent);

        return DeleteParentResponse.of(parent);
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
}