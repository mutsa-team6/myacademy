package com.project.myacademy.domain.file.academyprofile;

import com.project.myacademy.domain.file.academyprofile.dto.CreateAcademyProfileResponse;
import com.project.myacademy.domain.file.academyprofile.dto.DeleteAcademyProfileResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileS3UploadService;
import com.project.myacademy.domain.file.employeeprofile.dto.CreateEmployeeProfileResponse;
import com.project.myacademy.domain.file.employeeprofile.dto.DeleteEmployeeProfileResponse;
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

@Tag(name = "01-2. 학원", description = "학원 사진 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class AcademyProfileController {

    private final AcademyProfileS3UploadService academyProfileS3UploadService;

    // S3에 파일 업로드
    @Operation(summary = "학원 사진등록", description = "ADMIN 회원만 등록이 가능합니다. \n\n AWS S3서버에 저장됩니다.")
    @PostMapping("/{academyId}/files/upload")
    public ResponseEntity<Response<CreateAcademyProfileResponse>> upload(@PathVariable("academyId") Long academyId,
                                                                         @RequestPart MultipartFile multipartFile,
                                                                         Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateAcademyProfileResponse profileResponse = academyProfileS3UploadService.uploadAcademyProfile(academyId, multipartFile, requestAccount);
        return ResponseEntity.ok().body(Response.success(profileResponse));
    }


    // S3 파일 삭제
    @Operation(summary = "학원 사진삭제", description = "ADMIN 회원만 삭제가 가능합니다. \n\n db에는 soft-delete 되고, AWS S3서버에서는 삭제됩니다.")
    @DeleteMapping("/{academyId}/academyProfiles/{academyProfileId}/files")
    public ResponseEntity<Response<DeleteAcademyProfileResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                         @PathVariable("academyProfileId") Long academyProfileId,
                                                                         @RequestParam String filePath,
                                                                         Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteAcademyProfileResponse response = academyProfileS3UploadService.deleteAcademyProfile(academyId, academyProfileId, filePath, requestAccount);
        return ResponseEntity.ok().body(Response.success(response));
    }

    // S3 파일 다운로드
    @Operation(summary = "학원 사진다운로드", description = "인증된 모든 회원은 다운로드 가능합니다. \n\n AWS S3서버에 저장된 사진을 다운로드합니다.")
    @GetMapping("/{academyId}/files/download")
    public ResponseEntity<byte[]> download(@PathVariable("academyId") Long academyId,
                                           @RequestParam String fileUrl,
                                           Authentication authentication) throws IOException {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        // 버킷 폴더 이하 경로
        String filePath = fileUrl.substring(56);
        log.info("filePath : {}", filePath);
        return academyProfileS3UploadService.downloadAcademyProfile(academyId, filePath, requestAccount);
    }
}
