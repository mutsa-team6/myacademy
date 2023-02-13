package com.project.myacademy.domain.file.announcementfile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.academy.AcademyRepository;
import com.project.myacademy.domain.announcement.Announcement;
import com.project.myacademy.domain.announcement.AnnouncementRepository;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.file.announcementfile.dto.CreateAnnouncementFileResponse;
import com.project.myacademy.domain.file.announcementfile.dto.DeleteAnnoucementFileResponse;
import com.project.myacademy.domain.file.announcementfile.dto.ReadAnnouncementFilesResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnnouncementFileS3UploadService {

    private final AmazonS3Client amazonS3Client;
    private final AcademyRepository academyRepository;
    private final EmployeeRepository employeeRepository;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementFileRepository announcementFileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 공지사항 첨부파일 업로드
     *
     * @param academyId      학원 id
     * @param announcementId 파일 업로드 대상인 공지사항 id
     * @param multipartFile  파일 업로드를 위한 객체
     * @param account        파일 업로드 진행하는 직원 계정
     */
    public CreateAnnouncementFileResponse uploadAnnouncementFile(Long academyId, Long announcementId, List<MultipartFile> multipartFile, String account) {

        // 파일 자체를 첨부안하거나 첨부해도 내용이 비어있는지 확인
        validateFileExists(multipartFile);

        // 학원, 해당 학원 소속 직원 존재 유무 확인
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 업로드 권한 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 공지사항 존재 유무 확인
        Announcement announcement = validateAnnouncementById(announcementId);

        // 원본 파일 이름, S3에 저장될 파일 이름 리스트
        List<String> originalFileNameList = new ArrayList<>();
        List<String> storedFileNameList = new ArrayList<>();

        multipartFile.forEach(file -> {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());

            String originalFilename = file.getOriginalFilename();

            // file 형식이 잘못된 경우를 확인
            int index;
            try {
                index = originalFilename.lastIndexOf(".");
            } catch (NullPointerException | StringIndexOutOfBoundsException e) {
                throw new AppException(ErrorCode.WRONG_FILE_FORMAT);
            }

            String ext = originalFilename.substring(index + 1);

            // 저장될 파일 이름
            String storedFileName = UUID.randomUUID() + "." + ext;

            // 저장할 디렉토리 경로 + 파일 이름
            String key = "announcement/" + storedFileName;

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
            }

            String storeFileUrl = amazonS3Client.getUrl(bucket, key).toString();
            AnnouncementFile announcementFile = AnnouncementFile.makeAnnouncementFile(originalFilename, storeFileUrl, announcement);
            announcementFileRepository.save(announcementFile);

            storedFileNameList.add(storedFileName);
            originalFileNameList.add(originalFilename);

        });

        log.info("파일 등록 완료");
        return CreateAnnouncementFileResponse.of(originalFileNameList, storedFileNameList);
    }

    /**
     * 공지사항 첨부파일 삭제
     *
     * @param academyId          학원 id
     * @param announcementId     파일 업로드 대상인 공지사항 id
     * @param announcementFileId 공지사항 파일 id
     * @param filePath           S3버킷 폴더 이하의 디렉토리 경로
     * @param account            파일 삭제 진행하는 직원 계정
     */
    public DeleteAnnoucementFileResponse deleteAnnouncementFile(Long academyId, Long announcementId, Long announcementFileId, String filePath, String account) {

        // 학원, 해당 학원 소속 직원 존재 유무 확인
        Academy academy = validateAcademyById(academyId);
        Employee employee = validateRequestEmployeeByAcademy(account, academy);

        // 업로드 권한 확인(강사만 불가능)
        if (Employee.isTeacherAuthority(employee)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }

        // 공지사항 존재 유무 확인
        validateAnnouncementById(announcementId);

        // 공지사항 파일 존재 유무 확인
        AnnouncementFile announcementFile = validateAnnouncementFileById(announcementFileId);

        try {
            // S3 업로드 파일 삭제
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, filePath));
            // 해당 업로드 파일 테이블에서도 같이 삭제
            announcementFileRepository.delete(announcementFile);
            log.info("파일 삭제 성공");
        } catch (SdkClientException e) {
            log.error("파일 삭제 실패");
        }

        return DeleteAnnoucementFileResponse.of(employee);
    }

    /**
     * 공지사항 첨부파일 다운로드
     *
     * @param academyId      학원 id
     * @param announcementId 파일 업로드 대상인 공지사항 id
     * @param fileUrl        S3버킷에 저장된 객체의 전체 URL
     * @param account        파일 다운로드 진행하는 직원 계정
     */
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadAnnouncementFile(Long academyId, Long announcementId, String fileUrl, String account) throws IOException {

        // 학원, 해당 학원 소속 직원 존재 유무 확인
        Academy academy = validateAcademyById(academyId);
        validateRequestEmployeeByAcademy(account, academy);

        // 공지사항 존재 유무 확인
        validateAnnouncementById(announcementId);

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

        // 버킷 폴더에 저장된 해당 파일명 추출
        String fileName = bucketFolder[bucketFolder.length - 1];
        log.info("fileName : {}", fileName);

        // 인코딩
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        log.info("encodedFileName : {}", encodedFileName);

        httpHeaders.setContentDispositionFormData("attachment", encodedFileName);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }


    /**
     * s3 버킷에 저장된 프로필 객체 url 가져오는 메서드
     *
     * @param announcementId 찾고자 하는 직원의 id
     */
    @Transactional(readOnly = true)
    public List<ReadAnnouncementFilesResponse> getStoredUrls(Long announcementId) {

        return announcementFileRepository.findByAnnouncement_Id(announcementId)
                .stream().map(announcementFile -> new ReadAnnouncementFilesResponse(announcementFile.getStoredFileUrl(), announcementFile.getUploadFileName())).collect(Collectors.toList());

    }

    // 학원 Id로 학원을 조회 - 없을시 ACADEMY_NOT_FOUND 에러발생
    private Academy validateAcademyById(Long academyId) {
        return academyRepository.findById(academyId)
                .orElseThrow(() -> new AppException(ErrorCode.ACADEMY_NOT_FOUND));
    }

    // 요청하는 계정과 학원으로 직원을 조회 - 없을시 REQUEST_EMPLOYEE_NOT_FOUND 에러발생
    public Employee validateRequestEmployeeByAcademy(String account, Academy academy) {
        return employeeRepository.findByAccountAndAcademy(account, academy)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_EMPLOYEE_NOT_FOUND));
    }

    // 공지사항 Id로 공지사항 조회 - 없으면 ANNOUNCEMENT_NOT_FOUND 에러발생
    private Announcement validateAnnouncementById(Long announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_NOT_FOUND));
    }

    // 공지사항파일 Id로 공지사항파일 조회 - 없으면 ANNOUNCEMENT_FILE_NOT_FOUND 에러발생
    private AnnouncementFile validateAnnouncementFileById(Long announcementFileId) {
        return announcementFileRepository.findById(announcementFileId)
                .orElseThrow(() -> new AppException(ErrorCode.ANNOUNCEMENT_FILE_NOT_FOUND));
    }

    // 빈 파일이 아닌지 확인, 파일 자체를 첨부안하거나 첨부해도 내용이 비어있는지 확인 - 비어있으면 FILE_NOT_EXISTS 에러발생
    private void validateFileExists(List<MultipartFile> multipartFile) {
        for (MultipartFile mf : multipartFile) {
            if (mf.isEmpty()) {
                throw new AppException(ErrorCode.FILE_NOT_EXISTS);
            }
        }
    }

    // 저장된 파일 확장자 별로 구분하여 저장
    private MediaType contentType(String keyname) {
        String[] arr = keyname.split("\\.");
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
