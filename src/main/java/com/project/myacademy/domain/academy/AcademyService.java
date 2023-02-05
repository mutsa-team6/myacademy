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

        // 학원 Id로 학원을 조회 - 존재한다면 DUPLICATE_ACADEMY 에러발생
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

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);

        academyRepository.delete(academy);

        return academyId;
    }

    /**
     * 학원 이름으로 학원 조회
     *
     * @param request 찾을 학원의 이름이 담긴 dto
     */
    public FindAcademyResponse findAcademy(FindAcademyRequest request) {

        // 학원 이름으로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyByName(request.getName());

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;
    }

    /**
     * 해당 이름의 학원의 존재 여부
     *
     * @param academyName 학원이름
     */
    public boolean checkExistByAcademyName(String academyName) {
        // 해당이름의 학원이 존재하면 true 없으면 false 반환
        return academyRepository.existsByName(academyName);
    }

    /**
     * 학원Id로 학원 조회
     *
     * @param academyId 찾을 학원의 id
     */
    public FindAcademyResponse findAcademyById(Long academyId) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;
    }

    /**
     * 모든학원 조회
     */
    public Page<ReadAcademyResponse> readAllAcademies(Pageable pageable) {

        return academyRepository.findAll(pageable).map(academy -> new ReadAcademyResponse(academy));
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