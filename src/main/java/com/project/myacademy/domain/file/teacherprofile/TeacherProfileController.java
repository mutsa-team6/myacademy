package com.project.myacademy.domain.file.teacherprofile;

import com.project.myacademy.domain.file.teacherprofile.dto.CreateTeacherProfileResponse;
import com.project.myacademy.domain.file.teacherprofile.dto.DeleteTeacherProfileResponse;
import com.project.myacademy.global.Response;
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
@Tag(name = "7-2. 강사", description = "강사 사진 등록,수정,조회")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/academies")
public class TeacherProfileController {

    private final TeacherProfileS3UploadService teacherProfileS3UploadService;

    // S3에 파일 업로드
    @Operation(summary = "강사 사진등록", description = "ADMIN,STAFF 회원만 등록이 가능합니다. \n\n AWS S3서버에 저장됩니다.")
    @PostMapping("/{academyId}/teachers/{teacherId}/files/upload")
    public ResponseEntity<Response<CreateTeacherProfileResponse>> upload(@PathVariable("academyId") Long academyId,
                                                                         @PathVariable("teacherId") Long teacherId,
                                                                         @RequestPart List<MultipartFile> multipartFile,
                                                                         Authentication authentication) throws IOException {
        return ResponseEntity.ok().body(Response.success(teacherProfileS3UploadService.UploadTeacherProfile(academyId, teacherId, multipartFile, authentication.getName())));
    }


    // S3 파일 삭제
    @Operation(summary = "강사 사진삭제", description = "ADMIN,STAFF 회원만 삭제가 가능합니다. \n\n db에는 soft-delete 되고, AWS S3서버에서는 삭제됩니다.")
    @DeleteMapping("/{academyId}/teachers/{teacherId}/teacherProfiles/{teacherProfileId}/files")
    public ResponseEntity<Response<DeleteTeacherProfileResponse>> delete(@PathVariable("academyId") Long academyId,
                                                                         @PathVariable("teacherId") Long teacherId,
                                                                         @PathVariable("teacherProfileId") Long teacherProfileId,
                                                                         @RequestParam String filePath,
                                                                         Authentication authentication) {
        return ResponseEntity.ok().body(Response.success(teacherProfileS3UploadService.deleteTeacherProfile(academyId, teacherId, teacherProfileId, filePath, authentication.getName())));
    }

    // S3 파일 다운로드
    @Operation(summary = "강사 사진다운로드", description = "ADMIN,STAFF 회원만 다운로드가 가능합니다. \n\n AWS S3서버에 저장된 사진을 다운로드합니다.")
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