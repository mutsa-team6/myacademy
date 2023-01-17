package com.project.myacademy.domain.academy.service;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.domain.academy.entity.Academy;
import com.project.myacademy.domain.academy.repository.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
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
    private final EmployeeRepository employeeRepository;
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

    /**
     * 학원 정보 수정
     *
     * @param academyId
     * @param reqeust
     * @param name
     * @return AcademyDto
     */
    @Transactional
    public AcademyDto updateAcademy(Long academyId, UpdateAcademyReqeust reqeust, String name) {

        // 인증 확인
        log.info("인증정보로 저장소에서 계정정보를 확인합니다.");
        Employee employee = employeeRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, ErrorCode.ACCOUNT_NOT_FOUND.getMessage()));
        log.info("계정정보가 확인되었습니다.");

        // academyId로 학원정보  확인
        log.info("academyId로 저장소에서 학원정보를 조회합니다.");
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND, ErrorCode.ACADEMY_NOT_FOUND.getMessage()));
        log.info("저장소에서 학원정보가 조회되었습니다.");

        // 권한 확인
        log.info("학원정보의 소유자와 인증정보로 권한을 확인합니다.");
        if(!academy.getOwner().equals(name) && !employee.getEmployeeRole().equals("ADMIN")) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, ErrorCode.INVALID_PERMISSION.getMessage());
        }
        log.info("권한이 확인되었습니다.");

        // 학원 정보 수정
        log.info("학원정보를 수정하겠습니다.");
        academy.update(reqeust);
        log.info("학원정보를 수정하였습니다.");

        // 학원 정보 저장
        log.info("수정된 학원정보를 저장하겠습니다.");
        Academy updatedAcademy = academyRepository.save(academy);
        log.info("수정된 학원정보를 저장하겠습니다.");

        return updatedAcademy.toAcademyDto();
    }

    /**
     * 학원 삭제
     *
     * @param academyId
     * @return Long
     */
    @Transactional
    public Long deleteAcademy(Long academyId, String name) {

        // 인증 확인
        log.info("인증정보로 저장소에서 계정정보를 확인합니다.");
        Employee employee = employeeRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND, ErrorCode.ACCOUNT_NOT_FOUND.getMessage()));
        log.info("계정정보가 확인되었습니다.");

        // academyId로 학원정보  확인
        log.info("academyId로 저장소에서 학원정보를 조회합니다.");
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND, ErrorCode.ACADEMY_NOT_FOUND.getMessage()));
        log.info("저장소에서 학원정보가 조회되었습니다.");

        // 권한 확인
        log.info("학원정보의 소유자와 인증정보로 권한을 확인합니다.");
        if(!academy.getOwner().equals(name) && !employee.getEmployeeRole().equals("ADMIN")) {
            throw new AppException(ErrorCode.INVALID_PERMISSION, ErrorCode.INVALID_PERMISSION.getMessage());
        }
        log.info("권한이 확인되었습니다.");

        // 학원 정보 수정
        log.info("학원정보를 삭제하겠습니다.");
        academyRepository.delete(academy);
        log.info("학원정보를 삭제하였습니다.");

        return academyId;
    }

}
