package com.project.myacademy.domain.file.academyprofile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.file.academyprofile.dto.CreateAcademyProfileResponse;
import com.project.myacademy.domain.file.academyprofile.dto.DeleteAcademyProfileResponse;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfile;
import com.project.myacademy.domain.file.employeeprofile.EmployeeProfileRepository;
import com.project.myacademy.domain.file.employeeprofile.dto.CreateEmployeeProfileResponse;
import com.project.myacademy.domain.file.employeeprofile.dto.DeleteEmployeeProfileResponse;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyProfileS3UploadService {

    private final AmazonS3Client amazonS3Client;
    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final AcademyProfileRepository academyProfileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 학원 프로필 업로드
     *
     * @param academyId     파일 업로드 대상 학원 id
     * @param multipartFile 파일 업로드를 위한 객체
     * @param account       파일 업로드 진행하는 직원 계정
     */
    public CreateAcademyProfileResponse uploadAcademyProfile(Long academyId, MultipartFile multipartFile, String account) {

        // 빈 파일이 아닌지 확인, 파일 자체를 첨부안하거나 첨부해도 내용이 비어있으면 - FILE_NOT_EXISTS 에러발생
        validateFileExists(multipartFile);
        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 원장만 학원 프로필 등록 가능 - 아닐시 INVALID_PERMISSION 에러발생
        if (!employee.getEmployeeRole().equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        // 업로드한 파일 이름
        String originalFilename = multipartFile.getOriginalFilename();

        // file 형식이 잘못된 경우를 확인
        int index;
        try {
            index = originalFilename.lastIndexOf(".");
        } catch (StringIndexOutOfBoundsException e) {
            throw new AppException(ErrorCode.WRONG_FILE_FORMAT);
        }

        String ext = originalFilename.substring(index + 1);

        // 저장될 파일 이름
        String storedFileName = UUID.randomUUID() + "." + ext;

        // 저장할 디렉토리 경로 + 파일 이름
        String key = "academy/" + storedFileName;

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        // 저장될 프로필의 url
        String storeFileUrl = amazonS3Client.getUrl(bucket, key).toString();

        // 만약 해당 학원의 기존 프로필이 존재하는 경우
        academyProfileRepository.findByAcademy_Id(academy.getId())
                .ifPresent(academyProfile -> {
                    // 기존 프로필 객체 url 가져오기
                    String oldFileUrl = academyProfile.getStoredFileUrl();
                    String oldFilePath = oldFileUrl.substring(52);
                    // s3 버킷에서 기존 프로필 삭제하기
                    amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, oldFilePath));
                    // db에서도 삭제하기
                    academyProfileRepository.delete(academyProfile);
                });

        // 프로필 db에 저장하기
        AcademyProfile newAcademyProfile = AcademyProfile.makeAcademyProfile(originalFilename, storeFileUrl, academy);
        academyProfileRepository.save(newAcademyProfile);

        log.info("파일 등록 완료");
        return CreateAcademyProfileResponse.of(originalFilename, storedFileName);
    }

    /**
     * s3 버킷에 저장된 학원 프로필 객체 url 가져오는 메서드
     *
     * @param academyId 찾고자 하는 학원 id
     */
    public String getStoredUrl(Long academyId) {

        if (academyProfileRepository.existsAcademyProfileByAcademy_Id(academyId)) {
            return academyProfileRepository.findByAcademy_Id(academyId).get().getStoredFileUrl();
        } else {
            return "null";
        }
    }

    /**
     * 학원 프로필 삭제
     *
     * @param academyId         파일 업로드 대상 학원 id
     * @param academyProfileId  학원 파일 id
     * @param filePath          S3버킷 폴더 이하의 디렉토리 경로
     * @param account           파일 삭제 진행하는 직원 계정
     */
    public DeleteAcademyProfileResponse deleteAcademyProfile(Long academyId, Long academyProfileId, String filePath, String account) {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 원장만 파일 삭제할 권한 있음
        if (!employee.getEmployeeRole().equals(EmployeeRole.ROLE_ADMIN)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 학원프로필 아이디로 직원 프로필 조회 - 없을시 ACADEMY_PROFILE_NOT_FOUND 에러발생
        AcademyProfile academyProfile = validateAcademyProfileById(academyProfileId);

        try {
            // S3 업로드 파일 삭제
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
            // 해당 업로드 파일 테이블에서도 같이 삭제
            academyProfileRepository.delete(academyProfile);
            log.info("파일 삭제 성공");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
        return DeleteAcademyProfileResponse.of(academy);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadAcademyProfile(Long academyId, String fileUrl, String account) throws IOException {

        // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
        Academy academy = validateAcademyById(academyId);
        // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
        validateRequestEmployeeByAcademy(account, academy);

        // S3 객체 추출해서 byte 배열로 변환
        S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucket, fileUrl));
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        // 에러 처리해서 IOException 처리해줘야 함
        byte[] bytes = IOUtils.toByteArray(s3ObjectInputStream);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(contentType(fileUrl));
        httpHeaders.setContentLength(bytes.length);

        // 버킷 폴더 추출
        String[] bucketFolder = fileUrl.split("/");
//        log.info("bucketFolder : {}", bucketFolder);

        // 버킷 폴더에 저장된 해당 파일명 추출
        String fileName = bucketFolder[bucketFolder.length - 1];
        log.info("fileName : {}", fileName);

        // 인코딩
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        log.info("encodedFileName : {}", encodedFileName);

        httpHeaders.setContentDispositionFormData("attachment", encodedFileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        Academy validatedAcademy = academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
        return validatedAcademy;
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        Employee employee = employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
        return employee;
    }

    // 학원 프로필 아이디로 학원 프로필 조회 - 없을시 ACADEMY_PROFILE_NOT_FOUND 에러발생
    private AcademyProfile validateAcademyProfileById(Long academyProfileId) {
        AcademyProfile validatedAcademyProfile = academyProfileRepository.findById(academyProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_PROFILE_NOT_FOUND));
        return validatedAcademyProfile;
    }

    // 빈 파일이 아닌지 확인, 파일 자체를 첨부안하거나 첨부해도 내용이 비어있으면 - FILE_NOT_EXISTS 에러발생
    private void validateFileExists(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw new AppException(ErrorCode.FILE_NOT_EXISTS);
        }
    }

    // 저장된 파일 확장자 별로 구분하여 저장
    private MediaType contentType(String keyname) {
        String[] arr = keyname.split("\\.");
//        log.info("arr : {}", arr);
        String fileExtension = arr[arr.length - 1];
        switch (fileExtension) {
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
                return MediaType.IMAGE_JPEG;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
