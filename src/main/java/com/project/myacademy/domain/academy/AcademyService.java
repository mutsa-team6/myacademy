package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcademyService {

    private final AcademyRepository academyRepository;


    /**
     * 학원 등록
     *
     * @param request 이름, 주소, 폰번호, 원장이름, 사업자번호가 담긴 학원 등록 request
     */
    @Transactional
    public CreateAcademyResponse createAcademy(CreateAcademyRequest request) {

        //중복되는 학원이름은 등록 불가
        ifPresentAcademyByName(request.getName());

        Academy savedAcademy = academyRepository.save(Academy.createAcademy(request));
        log.info("✨ 학원 데이터 저장 성공");

        return new CreateAcademyResponse(savedAcademy);
    }

    /**
     * 학원 삭제
     *
     * @param academyId 학원 Id
     */
    @Transactional
    public Long deleteAcademy(Long academyId) {

        // 학원Id로 Academy 조회
        Academy academy = validateAcademyById(academyId);

        log.info("학원정보를 삭제하겠습니다.");
        academyRepository.delete(academy);
        log.info("학원정보를 삭제하였습니다.");

        return academyId;
    }

    /**
     * 학원 이름으로 학원 조회
     *
     * @param request 찾을 학원의 이름이 담긴 request
     */
    public FindAcademyResponse findAcademy(FindAcademyRequest request) {

        // 학원이름으로 Academy 조회
        Academy academy = validateAcademyByName(request.getName());

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;
    }

    /**
     * 학원Id로 학원 조회
     *
     * @param academyId 찾을 학원의 id
     */
    public FindAcademyResponse findAcademyById(Long academyId) {

        // 학원 Id로 학원 조회
        Academy academy = validateAcademyById(academyId);

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;
    }

    /**
     * 모든학원 조회
     *
     * @param pageable
     */
    public Page<ReadAcademyResponse> readAllAcademies(Pageable pageable) {

        return academyRepository.findAll(pageable).map(academy -> new ReadAcademyResponse(academy));
    }

    /**
     * 해당 이름의 학원의 존재 여부
     *
     * @param academyName 학원이름
     */
    public boolean checkExistByAcademyName(String academyName) {
        return academyRepository.existsByName(academyName);
    }

    //학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        Academy validateAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validateAcademy;
    }

    //학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyByName(String academyName) {
        Academy validateAcademy = academyRepository.findByName(academyName)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validateAcademy;
    }

    //학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private void ifPresentAcademyByName(String academyName) {
        academyRepository.findByName(academyName)
                .ifPresent(academy -> {throw new AppException(ErrorCode.DUPLICATED_ACADEMY);});
    }
}
//    /**
//     * 학원 등록
//     *
//     * @param request
//     * @return AcademyDto
//     */
//    @Transactional
//    public AcademyDto createAcademy(CreateAcademyRequest request) {
//
//        // 요청의 등록번호 추출
//        String businessRegistrationNumber = request.getBusinessRegistrationNumber();
//        log.info("요청에서 등록번호를 추출했습니다.");
//
//        // 등록번호로 저장소에서 찾아보고 중복 시 에러 발생
//        log.info("등록번호로 저장소에서 학원정보를 조회합니다.");
//        academyRepository.findByBusinessRegistrationNumber(businessRegistrationNumber)
//                .ifPresent(academy -> {
//                    throw new AppException(ErrorCode.DUPLICATED_ACADEMY);
//                });
//        log.info("학원정보를 저장소에서 조회했습니다.");
//
//        // 학원을 저장소에 등록
//        log.info("학원정보를 저장소에 등록합니다.");
//        Academy savedAcademy = academyRepository.save(request.toAcademy(bCryptPasswordEncoder.encode(request.getPassword())));
//        log.info("학원정보가 저장소에 등록되었습니다.");
//
//        return savedAcademy.toAcademyDto();
//    }

//    /**
//     * 학원 정보 수정
//     *
//     * @param academyId
//     * @param reqeust
//     * @param name
//     * @return AcademyDto
//     */
//    @Transactional
//    public AcademyDto updateAcademy(Long academyId, UpdateAcademyReqeust reqeust, String name) {
//
//        // 인증 확인
//        log.info("인증정보로 저장소에서 계정정보를 확인합니다.");
//        Employee employee = employeeRepository.findByName(name)
//                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
//        log.info("계정정보가 확인되었습니다.");
//
//        // academyId로 학원정보  확인
//        log.info("academyId로 저장소에서 학원정보를 조회합니다.");
//        Academy academy = academyRepository.findById(academyId)
//                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
//        log.info("저장소에서 학원정보가 조회되었습니다.");
//
//        // 권한 확인
//        log.info("학원정보의 소유자와 인증정보로 권한을 확인합니다.");
//        if (!academy.getOwner().equals(name) && !employee.getEmployeeRole().equals(EmployeeRole.ROLE_ADMIN)) {
//            throw new AppException(ErrorCode.INVALID_PERMISSION);
//        }
//        log.info("권한이 확인되었습니다.");
//
//        // 학원 정보 수정
//        log.info("학원정보를 수정하겠습니다.");
//        academy.update(reqeust);
//        log.info("학원정보를 수정하였습니다.");
//
//        // 학원 정보 저장
//        log.info("수정된 학원정보를 저장하겠습니다.");
//        Academy updatedAcademy = academyRepository.save(academy);
//        log.info("수정된 학원정보를 저장하겠습니다.");
//
//        return updatedAcademy.toAcademyDto();
//    }
