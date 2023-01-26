package com.project.myacademy.domain.file.teacherprofile;

import com.project.myacademy.domain.file.teacherprofile.dto.CreateTeacherProfileResponse;
import com.project.myacademy.domain.file.teacherprofile.dto.DeleteTeacherProfileResponse;
import com.project.myacademy.global.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class TeacherProfileController {

    private final TeacherProfileS3UploadService teacherProfileS3UploadService;

    // S3에 파일 업로드
    @PostMapping("/{academyId}/teachers/{teacherId}/files/upload")
    public ResponseEntity<Response<CreateTeacherProfileResponse>> upload(@PathVariable("academyId") Long academyId,
                                                                         @PathVariable("teacherId") Long teacherId,
                                                                         @RequestPart MultipartFile multipartFile,
                                                                         Authentication authentication) throws IOException {
        return ResponseEntity.ok().body(Response.success(teacherProfileS3UploadService.UploadTeacherProfile(academyId, teacherId, multipartFile, authentication.getName())));
    }


    // S3 파일 삭제
    @DeleteMapping("/{academyId}/teachers/{teacherId}/teacherProfiles/{teacherProfileId}/files")
    public ResponseEntity<Response<DeleteTeacherProfileResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                         @PathVariable("teacherId") Long teacherId,
                                                                         @PathVariable("teacherProfileId") Long teacherProfileId,
                                                                         @RequestParam String filePath,
                                                                         Authentication authentication) {
        return ResponseEntity.ok().body(Response.success(teacherProfileS3UploadService.deleteTeacherProfile(academyId, teacherId, teacherProfileId, filePath, authentication.getName())));
    }

    // S3 파일 다운로드
    @GetMapping("/{academyId}/teachers/{teacherId}/files/download")
    public ResponseEntity<byte[]> download(@PathVariable("academyId") Long academyId,
                                           @PathVariable("teacherId") Long teacherId,
                                           @RequestParam String fileUrl,
                                           Authentication authentication) throws IOException {
        // 버킷 폴더 이하 경로
        String filePath = fileUrl.substring(52);
        log.info("filePath : {}", filePath);
        return teacherProfileS3UploadService.downloadTeacherProfile(academyId, teacherId, filePath, authentication.getName());
    }

}