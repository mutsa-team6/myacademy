package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
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
     * 부모 등록
     *
     * @param academyId 학원 id
     * @param request   부모 정보가 담긴 request
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateParentResponse createParent(Long academyId, CreateParentRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        //부모 핸드폰번호와 academyId 로 중복되는 부모가 있는지 확인함.
        parentRepository.findByPhoneNumAndAcademyId(request.getPhoneNum(), academyId)
                .ifPresent(user -> {
                    throw new AppException(ErrorCode.DUPLICATED_PARENT);
                });

        Parent savedParent = parentRepository.save(Parent.toParent(request, academyId));

        return CreateParentResponse.of(savedParent);
    }

    /**
     * 부모 조회
     *
     * @param parentId  찾을 부모 Id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    public ReadParentResponse readParent(Long academyId, long parentId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

        return ReadParentResponse.of(parent);
    }

    /**
     * 부모 수정
     *
     * @param parentId  수정할 부모 Id
     * @param academyId 학원 id
     * @param request   수정할 부모 정보를 담은 requestDto
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateParentResponse updateParent(Long academyId, Long parentId, UpdateParentRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

        parent.updateParent(request);

        return UpdateParentResponse.of(parent);
    }

    /**
     * 부모 삭제
     *
     * @param parentId  삭제할 부모 Id
     * @param academyId 학원 id
     * @param account   jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteParentResponse deleteParent(Long academyId, Long parentId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);
        //parentId와 academyId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = validateParent(parentId, academyId);

        parentRepository.delete(parent);

        return DeleteParentResponse.of(parent);
    }

    public boolean checkExistByPhoneAndAcademy(String parentPhoneNum, Long academyId) {
        return parentRepository.existsByPhoneNum(parentPhoneNum);
    }

    private Parent validateParent(Long parentId, Long academyId) {
        // parentId와 academyId에 해당하는 부모 존재 유무 확인
        Parent parent = parentRepository.findByIdAndAcademyId(parentId, academyId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));
        return parent;
    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    public Academy validateAcademyById(Long academyId) {
        return academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
        return employee;
    }

    // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
    public void validateAuthorityUser(Employee employee) {
        if (employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

}