package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.academy.dto.FindAcademyResponse;
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

        //부모 핸드폰번호와 academyId 로 중복되는 부모가 있는지 확인함.
        parentRepository.findByPhoneNumAndAcademyId(request.getPhoneNum(), academyId)
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_PARENT);
                });

        Parent savedParent = parentRepository.save(Parent.toParent(request, academyId));

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
        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

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
        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

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
        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

        parentRepository.delete(parent);

        return DeleteParentResponse.of(parent);
    }

    /**
     * 전화번호와 학원 id로 부모 잧는 메서드 ( UI 용)
     */
    public FindParentResponse findParent(String requestPhoneNum, Long academyId) {

        Parent foundParent = parentRepository.findByPhoneNumAndAcademyId(requestPhoneNum, academyId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        FindParentResponse response = new FindParentResponse(foundParent.getId(), foundParent.getName());

        return response;


    }

    private Parent validateParent(Long parentId, Long academyId) {
        // parentId와 academyId에 해당하는 부모 존재 유무 확인
        Parent parent = parentRepository.findByIdAndAcademyId(parentId, academyId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));
        return parent;
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