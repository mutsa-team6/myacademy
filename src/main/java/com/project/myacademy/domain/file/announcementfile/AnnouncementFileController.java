package com.project.myacademy.domain.file.announcementfile;

import com.project.myacademy.domain.file.announcementfile.dto.CreateAnnouncementFileResponse;
import com.project.myacademy.domain.file.announcementfile.dto.DeleteAnnoucementFileResponse;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "03-2. 학원 공지사항", description = "학원 공지사항 첨부파일 등록,삭제,다운로드")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class AnnouncementFileController {

    private final AnnouncementFileS3UploadService announcementFileS3UploadService;

    // S3에 파일 업로드
    @Operation(summary = "공지사항 첨부파일 등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다. \n\n AWS S3서버에 저장됩니다.")
    @PostMapping("/{academyId}/announcements/{announcementId}/files/upload")
    public ResponseEntity<Response<CreateAnnouncementFileResponse>> upload(@PathVariable("academyId") Long academyId,
                                                                           @PathVariable("announcementId") Long announcementId,
                                                                           @RequestPart List<MultipartFile> multipartFile,
                                                                           Authentication authentication) throws IOException {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateAnnouncementFileResponse fileResponse = announcementFileS3UploadService.UploadAnnouncementFile(academyId, announcementId, multipartFile, requestAccount);
        return ResponseEntity.ok().body(Response.success(fileResponse));
    }


    // S3 파일 삭제
    @Operation(summary = "공지사항 첨부파일 삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n db에는 soft-delete 되고, AWS S3서버에서는 삭제됩니다.")
    @DeleteMapping("/{academyId}/announcements/{announcementId}/announcementFiles/{announcementFileId}/files")
    public ResponseEntity<Response<DeleteAnnoucementFileResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                          @PathVariable("announcementId") Long announcementId,
                                                                          @PathVariable("announcementFileId") Long announcementFileId,
                                                                          @RequestParam String filePath,
                                                                          Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteAnnoucementFileResponse fileResponse = announcementFileS3UploadService.deleteAnnouncementFile(academyId, announcementId, announcementFileId, filePath, requestAccount);
        return ResponseEntity.ok().body(Response.success(fileResponse));
    }

    // S3 파일 다운로드
    @Operation(summary = "공지사항 첨부파일 다운로드", description = "인증된 모든 회원은 다운로드 가능합니다. \n\n AWS S3서버에 저장된 파일을 다운로드합니다.")
    @GetMapping("/{academyId}/announcements/{announcementId}/files/download")
    public ResponseEntity<byte[]> download(@PathVariable("academyId") Long academyId,
                                           @PathVariable("announcementId") Long announcementId,
                                           @RequestParam String fileUrl,
                                           Authentication authentication) throws IOException {

        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        // 버킷 폴더 이하 경로
        String filePath = fileUrl.substring(56);
        log.info("filePath : {}", filePath);
        return announcementFileS3UploadService.downloadAnnouncementFile(academyId, announcementId, filePath, requestAccount);
    }
}
