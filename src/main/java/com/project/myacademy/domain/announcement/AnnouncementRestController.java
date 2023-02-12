package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.announcement.dto.*;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.exception.BindingException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03-1. 학원 공지사항", description = "학원 공지사항 작성,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class AnnouncementRestController {

    private final AnnouncementService announcementService;

    /**
     * 특정 학원 공지사항 작성
     */
    @Operation(summary = "공지사항 작성", description = "ADMIN,STAFF 회원만 작성이 가능합니다. \n\n TYPE 종류, 1. 일반 공지사항 = ANNOUNCEMENT, 2. 입시 정보 = ADMISSION")
    @PostMapping("/{academyId}/announcements")
    public ResponseEntity<Response<CreateAnnouncementResponse>> create(@PathVariable Long academyId, @Validated @RequestBody CreateAnnouncementRequest request, BindingResult bindingResult, Authentication authentication) {

        if (bindingResult.hasFieldErrors()) {
            throw new BindingException(ErrorCode.BINDING_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateAnnouncementResponse response = announcementService.createAnnouncement(academyId, requestAccount, request);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학원 모든 공지사항 목록 조회
     */
    @Operation(summary = "모든 공지사항 전체 조회", description = "모든 공지사항을 전체 조회합니다.")
    @GetMapping("/{academyId}/announcements")
    public ResponseEntity<Response<Page<ReadAllAnnouncementResponse>>> readAll(@PathVariable Long academyId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllAnnouncementResponse> responses = announcementService.readAllAnnouncement(academyId, pageable, requestAccount);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 특정 학원 type = 공지사항 타입에 따른 조회
     */
    @Operation(summary = "공지사항 타입별 전체 조회", description = "TYPE 별로 공지사항을 전체 조회합니다. \n\n TYPE 종류, 1. 일반 공지사항 = ANNOUNCEMENT, 2. 입시 정보 = ADMISSION")
    @GetMapping("/{academyId}/announcements/type/{announcementType}")
    public ResponseEntity<Response<Page<ReadAllAnnouncementResponse>>> readAllByType(@PathVariable Long academyId, @PathVariable String announcementType, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by("id").descending());
        Page<ReadAllAnnouncementResponse> responses = announcementService.readTypeAnnouncement(academyId, pageable, requestAccount, announcementType);
        return ResponseEntity.ok().body(Response.success(responses));
    }

    /**
     * 특정 학원 공지사항 단건 조회
     */
    @Operation(summary = "공지사항 단건 조회", description = "공지사항을 단건 조회합니다.")
    @GetMapping("/{academyId}/announcements/{announcementId}")
    public ResponseEntity<Response<ReadAnnouncementResponse>> read(@PathVariable Long academyId, @PathVariable Long announcementId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        ReadAnnouncementResponse response = announcementService.readAnnouncement(academyId, announcementId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학원 공지사항 수정
     */
    @Operation(summary = "공지사항 수정", description = "ADMIN,STAFF 회원만 수정이 가능합니다.")
    @PutMapping("/{academyId}/announcements/{announcementId}")
    public ResponseEntity<Response<UpdateAnnouncementResponse>> update(@PathVariable Long academyId, @PathVariable Long announcementId, @Validated @RequestBody UpdateAnnouncementRequest request, BindingResult bindingResult, Authentication authentication) {

        if (bindingResult.hasFieldErrors()) {
            throw new BindingException(ErrorCode.BINDING_ERROR, bindingResult.getFieldError().getDefaultMessage());
        }

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        UpdateAnnouncementResponse response = announcementService.updateAnnouncement(academyId, announcementId, request, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    /**
     * 특정 학원 공지사항 삭제
     */
    @Operation(summary = "공지사항 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다.")
    @DeleteMapping("/{academyId}/announcements/{announcementId}")
    public ResponseEntity<Response<DeleteAnnouncementResponse>> delete(@PathVariable Long academyId, @PathVariable Long announcementId, Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteAnnouncementResponse response = announcementService.deleteAnnouncement(academyId, announcementId, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }
}
