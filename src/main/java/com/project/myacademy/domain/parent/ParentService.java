package com.project.myacademy.domain.parent;

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
public class ParentService {

    private final ParentRepository parentRepository;
    private final EmployeeRepository employeeRepository;

    /**
     *
     * @param request 부모 정보가 담긴 request
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public CreateParentResponse createParent(CreateParentRequest request, String account) {

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
     *
     * @param parentId 찾을 부모 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    public FindParentResponse findParent(long parentId, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //parentId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        return FindParentResponse.of(parent);
    }

    /**
     *
     * @param parentId 수정할 부모 Id
     * @param request 수정할 부모 정보를 담은 requestDto
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public UpdateParentResponse updateParent(Long parentId, UpdateParentRequest request, String account) {

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
     *
     * @param parentId 삭제할 부모 Id
     * @param account jwt로 받아온 사용자(Employee) 계정
     */
    @Transactional
    public DeleteParentResponse deleteParent(Long parentId, String account) {

        //JWT에서 받은 Employee account가 존재하는지 확인
        employeeRepository.findByAccount(account)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        //parentId에 해당하는 부모 정보가 존재하는지 확인
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_NOT_FOUND));

        parentRepository.delete(parent);

        return DeleteParentResponse.of(parent);
    }
}