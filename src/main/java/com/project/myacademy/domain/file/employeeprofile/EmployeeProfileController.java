package com.project.myacademy.domain.file.employeeprofile;

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
import java.util.List;

@Tag(name = "02-3. 직원", description = "직원 사진 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class EmployeeProfileController {

    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;

    // S3에 파일 업로드
    @Operation(summary = "직원 사진등록", description = "STAFF 회원은 본인만, ADMIN 회원은 모든 STAFF 관련 등록이 가능합니다. \n\n AWS S3서버에 저장됩니다.")
    @PostMapping("/{academyId}/employees/{employeeId}/files/upload")
    public ResponseEntity<Response<CreateEmployeeProfileResponse>> upload(@PathVariable("academyId") Long academyId,
                                                                          @PathVariable("employeeId") Long employeeId,
                                                                          @RequestPart List<MultipartFile> multipartFile,
                                                                          Authentication authentication) throws IOException {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        CreateEmployeeProfileResponse profileResponse = employeeProfileS3UploadService.uploadEmployeeProfile(academyId, employeeId, multipartFile, requestAccount);
        return ResponseEntity.ok().body(Response.success(profileResponse));
    }


    // S3 파일 삭제
    @Operation(summary = "직원 사진삭제", description = "STAFF 회원은 본인 관련만, ADMIN 회원은 모든 STAFF 관련 삭제가 가능합니다. \n\n db에는 soft-delete 되고, AWS S3서버에서는 삭제됩니다.")
    @DeleteMapping("/{academyId}/employees/{employeeId}/employeeProfiles/{employeeProfileId}/files")
    public ResponseEntity<Response<DeleteEmployeeProfileResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                          @PathVariable("employeeId") Long employeeId,
                                                                          @PathVariable("employeeProfileId") Long employeeProfileId,
                                                                          @RequestParam String filePath,
                                                                          Authentication authentication) {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        DeleteEmployeeProfileResponse profileResponse = employeeProfileS3UploadService.deleteEmployeeProfile(academyId, employeeId, employeeProfileId, filePath, requestAccount);
        return ResponseEntity.ok().body(Response.success(profileResponse));
    }

    // S3 파일 다운로드
    @Operation(summary = "직원 사진다운로드", description = "인증된 모든 회원은 다운로드 가능합니다. \n\n AWS S3서버에 저장된 사진을 다운로드합니다.")
    @GetMapping("/{academyId}/employees/{employeeId}/files/download")
    public ResponseEntity<byte[]> download(@PathVariable("academyId") Long academyId,
                                           @PathVariable("employeeId") Long employeeId,
                                           @RequestParam String fileUrl,
                                           Authentication authentication) throws IOException {
        String requestAccount = AuthenticationUtil.getAccountFromAuth(authentication);
        // 버킷 폴더 이하 경로
        String filePath = fileUrl.substring(52);
        log.info("filePath : {}", filePath);
        return employeeProfileS3UploadService.downloadEmployeeProfile(academyId, employeeId, filePath, requestAccount);
    }
}