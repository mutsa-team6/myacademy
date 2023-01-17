package com.project.myacademy.domain.parent;

import com.project.myacademy.domain.parent.dto.CreateParentRequest;
import com.project.myacademy.domain.parent.dto.CreateParentResponse;
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

    /**
     * 부모 등록
     */
    @Transactional
    public CreateParentResponse createParent(CreateParentRequest request) {
        //중복체크
        parentRepository.findByPhoneNum(request.getPhoneNum())
                .ifPresent(user -> {throw new AppException(ErrorCode.DUPLICATED_PARENT);});

        Parent savedParent = parentRepository.save(Parent.toParent(request));

        return CreateParentResponse.of(savedParent);
    }
}