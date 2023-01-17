package com.project.myacademy.domain.academy.service;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.domain.academy.entity.Academy;
import com.project.myacademy.domain.academy.repository.AcademyRepository;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AcademyService {

    private final AcademyRepository academyRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 학원 등록
     *
     * @param request
     * @return AcademyDto
     */
    @Transactional
    public AcademyDto createAcademy(CreateAcademyRequest request) {

        // 요청의 등록번호 추출
        String businessRegistrationNumber = request.getBusinessRegistrationNumber();
        log.info("요청에서 등록번호를 추출했습니다.");

        // 등록번호로 저장소에서 찾아보고 중복 시 에러 발생
        log.info("등록번호로 저장소에서 학원정보를 조회합니다.");
        academyRepository.findByBusinessRegistrationNumber(businessRegistrationNumber)
                .ifPresent(academy -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACADEMY, ErrorCode.DUPLICATED_ACADEMY.getMessage());
                });
        log.info("학원정보를 저장소에서 조회했습니다.");

        // 학원을 저장소에 등록
        log.info("학원정보를 저장소에 등록합니다.");
        Academy savedAcademy = academyRepository.save(request.toAcademy(bCryptPasswordEncoder.encode(request.getPassword())));
        log.info("학원정보가 저장소에 등록되었습니다.");

        return savedAcademy.toAcademyDto();
    }
}
