package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.academy.dto.*;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.parent.ParentRepository;
import com.project.myacademy.domain.student.StudentRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AcademyService {

    private final AcademyRepository academyRepository;


//    private final EmployeeRepository employeeRepository;
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//
//    @Value("${jwt.token.secret}")
//    private String secretKey;
//    private long expiredTimeMs = 1000 * 60 * 60;

    /**
     * 학원 등록
     *
     * @param request
     * @return AcademyDto
     */
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
////        Academy savedAcademy = academyRepository.save(request.toAcademy(bCryptPasswordEncoder.encode(request.getPassword())));
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

    /**
     * 학원 삭제
     *
     * @param academyId
     * @return Long
     */
    @Transactional
    public Long deleteAcademy(Long academyId) {


        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));

        log.info("학원정보를 삭제하겠습니다.");
        academyRepository.delete(academy);
        log.info("학원정보를 삭제하였습니다.");

        return academyId;
    }



    public FindAcademyResponse findAcademy(FindAcademyRequest request) {

        // 검색하려는 학원 데이터가 존재하지 않음
        Academy academy = academyRepository.findByName(request.getName())
                .orElseThrow(() -> {
                    throw new AppException(ErrorCode.ACADEMY_NOT_FOUND);
                });

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;

    }

    public FindAcademyResponse findAcademyById(Long academyId) {

        // 검색하려는 학원 데이터가 존재하지 않음
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> {
                    throw new AppException(ErrorCode.ACADEMY_NOT_FOUND);
                });

        FindAcademyResponse response = new FindAcademyResponse(academy);

        return response;

    }

    public Page<ReadAcademyResponse> readAllAcademies(Pageable pageable) {

        return academyRepository.findAll(pageable).map(academy -> new ReadAcademyResponse(academy));
    }

    @Transactional
    public CreateAcademyResponse createAcademy(CreateAcademyRequest request) {

        //같은 이름 학원은 허용하지 않는다.
        academyRepository.findByName(request.getName())
                .ifPresent(academy -> {
                    throw new AppException(ErrorCode.DUPLICATED_ACADEMY);
                });

        Academy savedAcademy = academyRepository.save(Academy.createAcademy(request));
        log.info("✨ 학원 데이터 저장 성공");

        return new CreateAcademyResponse(savedAcademy);
    }

    public boolean checkExistByAcademyName(String academyName) {
        return academyRepository.existsByName(academyName);
    }
}
