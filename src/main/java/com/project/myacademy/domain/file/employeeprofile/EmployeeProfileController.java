package com.project.myacademy.domain.file.employeeprofile;

import com.project.myacademy.domain.file.employeeprofile.dto.CreateEmployeeProfileResponse;
import com.project.myacademy.domain.file.employeeprofile.dto.DeleteEmployeeProfileResponse;
import com.project.myacademy.global.Response;
import com.project.myacademy.global.util.AuthenticationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class EmployeeProfileController {

    private final EmployeeProfileS3UploadService employeeProfileS3UploadService;

    // S3에 파일 업로드
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