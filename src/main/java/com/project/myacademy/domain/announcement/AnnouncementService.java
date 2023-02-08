package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.announcement.dto.*;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnouncementService {

    private final EmployeeRepository employeeRepository;
    private final AnnouncementRepository announcementRepository;
    private final AcademyRepository academyRepository;


    /**
     * 공지사항 등록
     *
     * @param academyId 학원 id
     * @param request   공지사항 제목, 내용이 들어간 dto
     * @param account   직원 계정
     */
    public CreateAnnouncementResponse createAnnouncement(Long academyId, String account, CreateAnnouncementRequest request) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        Announcement savedAnnouncement = announcementRepository.save(Announcement.toAnnouncement(request, academy, employee));

        return CreateAnnouncementResponse.of(savedAnnouncement);
    }

    /**
     * 공지사항 전체 조회
     *
     * @param academyId 학원 id
     * @param account   직원 계정
     */
    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> readAllAnnouncement(Long academyId, Pageable pageable, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        return announcementRepository.findAllByAcademyOrderByCreatedAtDesc(academy, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
    }

    /**
     * 공지사항 타입별 조회
     *
     * @param academyId 학원 id
     * @param account   로그인한 계정
     * @param type      공지사항 타입 (ANNOUNCEMENT, ADMISSION)
     */
    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> readTypeAnnouncement(Long academyId, Pageable pageable, String account, String type) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        AnnouncementType announcementType = AnnouncementType.valueOf(type);

        return announcementRepository.findAllByTypeAndAcademy(announcementType, academy, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
    }


    /**
     * 공지사항 단건 조회
     *
     * @param academyId      학원 id
     * @param announcementId 조회하려는 특이사항의 Id
     * @param account        직원 계정
     */
    @Transactional(readOnly = true)
    public ReadAnnouncementResponse readAnnouncement(Long academyId, Long announcementId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        //announcementId에 해당하는 특이사항이 있는 지확인하고 있으면 가져옴
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        return ReadAnnouncementResponse.of(announcement);
    }

    /**
     * 공지사항 수정
     *
     * @param academyId      학원 id
     * @param announcementId 변경할 공지사항 id
     * @param request        변경할 내용과 제목을 담은 dto
     * @param account        직원 계정
     */
    public UpdateAnnouncementResponse updateAnnouncement(Long academyId, Long announcementId, @RequestBody UpdateAnnouncementRequest request, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        //announcementId에 해당하는 특이사항이 있는지 확인하고 있으면 가져옴
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcement.updateAnnouncement(request);

        return UpdateAnnouncementResponse.of(announcement);
    }

    /**
     * 공지사항 삭제
     *
     * @param academyId      학원 id
     * @param announcementId 삭제할 공지사항 id
     * @param account        직원 계정
     */
    public DeleteAnnouncementResponse deleteAnnouncement(Long academyId, Long announcementId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);
        // 해당 직원의 권한 체크 - USER 이면 INVALID_PERMISSION 에러발생
        validateAuthorityUser(employee);

        //announcementId에 해당하는 특이사항이 있는지 확인하고 있으면 가져옴
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcementRepository.delete(announcement);

        return DeleteAnnouncementResponse.of(announcement);
    }

    /**
     * 메인에 공지사항 5개 보여주기 위한 메서드
     */
    @Transactional(readOnly = true)
    public List<ReadAnnouncementResponse> readAnnouncementForMain(Long academyId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        List<Announcement> announcements = announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ANNOUNCEMENT, academy);

        return announcements.stream().map(announcement ->
                ReadAnnouncementResponse.of(announcement)).collect(Collectors.toList());
    }

    /**
     * 메인에 입시정보 5개 보여주기 위한 메서드
     */
    @Transactional(readOnly = true)
    public List<ReadAnnouncementResponse> readAdmissionForMain(Long academyId, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        return announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ADMISSION, academy).stream().map(announcement ->
                ReadAnnouncementResponse.of(announcement)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> searchAnnouncement(Long academyId, String title, Pageable pageable, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        return announcementRepository.findAllByAcademyAndTitleContainingOrderByCreatedAtDesc(academy, title, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
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
