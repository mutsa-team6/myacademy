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

    /**
     * 공지사항에 추가하면 좋을 것 같은 기능들 (방법은 생각안해봄)
     * 공지사항을 직원이 읽었을 경우 체크 할 수 있게
     * 공지사항의 유효기간을 만들고 이후에 자동으로 삭제되도록
     * 공지사항을 누가썼는지 알 수 있게
     * 권한별로 공지사항이 다르게 표시되게
     */
    private final EmployeeRepository employeeRepository;
    private final AnnouncementRepository announcementRepository;
    private final AcademyRepository academyRepository;


    /**
     * @param academyId 학원 id
     * @param request   공지사항 제목, 내용이 들어간 dto
     * @param account   직원 계정
     */
    public CreateAnnouncementResponse createAnnouncement(Long academyId, String account, CreateAnnouncementRequest request) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        // 공지사항을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        Announcement savedAnnouncement = announcementRepository.save(Announcement.toAnnouncement(request, academy, employee));

        return CreateAnnouncementResponse.of(savedAnnouncement);
    }

    /**
     * @param academyId 학원 id
     * @param pageable  20개씩 id순서대로(최신순)
     * @param account   직원 계정
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> readAllAnnouncement(Long academyId, Pageable pageable, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        return announcementRepository.findAllByAcademyOrderByCreatedAtDesc(academy, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
    }

    /**
     * @param academyId 학원 id
     * @param pageable  20개씩 id순서대로(최신순)
     * @param account   로그인한 계정
     * @param type      공지사항 타입 (ANNOUNCEMENT, ADMISSION)
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> readTypeAnnouncement(Long academyId, Pageable pageable, String account, String type) {
        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        AnnouncementType announcementType = AnnouncementType.valueOf(type);
        return announcementRepository.findAllByTypeAndAcademy(announcementType, academy, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
    }


    /**
     * @param academyId      학원 id
     * @param announcementId 조회하려는 특이사항의 Id
     * @param account        직원 계정
     */
    @Transactional(readOnly = true)
    public ReadAnnouncementResponse readAnnouncement(Long academyId, Long announcementId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        //announcementId에 해당하는 특이사항이 있는 지확인하고 있으면 가져옴
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        return ReadAnnouncementResponse.of(announcement);
    }

    /**
     * @param academyId      학원 id
     * @param announcementId 변경할 공지사항 id
     * @param request        변경할 내용과 제목을 담은 dto
     * @param account        직원 계정
     */
    public UpdateAnnouncementResponse updateAnnouncement(Long academyId, Long announcementId, @RequestBody UpdateAnnouncementRequest request, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        // 공지사항을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        //announcementId에 해당하는 특이사항이 있는지 확인하고 있으면 가져옴
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcement.updateAnnouncement(request);

        return UpdateAnnouncementResponse.of(announcement);
    }

    /**
     * @param academyId      학원 id
     * @param announcementId 삭제할 공지사항 id
     * @param account        직원 계정
     */
    public DeleteAnnouncementResponse deleteAnnouncement(Long academyId, Long announcementId, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);
        // 공지사항을 관리 할 수 있는 권한인지 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

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
        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        List<Announcement> announcements = announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ANNOUNCEMENT, academy);

        return announcements.stream().map(announcement ->
                ReadAnnouncementResponse.of(announcement)).collect(Collectors.toList());
    }

    /**
     * 메인에 입시정보 5개 보여주기 위한 메서드
     */
    @Transactional(readOnly = true)

    public List<ReadAnnouncementResponse> readAdmissionForMain(Long academyId, String account) {
        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        return announcementRepository.findTop5ByTypeAndAcademyOrderByCreatedAtDesc(AnnouncementType.ADMISSION, academy).stream().map(announcement ->
                ReadAnnouncementResponse.of(announcement)).collect(Collectors.toList());
    }

    /**
     *
     */
    @Transactional(readOnly = true)
    public Page<ReadAllAnnouncementResponse> searchAnnouncement(Long academyId, String title, Pageable pageable, String account) {

        //academyId 존재 유무 확인
        Academy academy = validateAcademy(academyId);
        //account 유효검사
        Employee employee = validateAcademyEmployee(account, academy);

        return announcementRepository.findAllByAcademyAndTitleContainingOrderByCreatedAtDesc(academy, title, pageable).map(announcement -> ReadAllAnnouncementResponse.of(announcement));
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
