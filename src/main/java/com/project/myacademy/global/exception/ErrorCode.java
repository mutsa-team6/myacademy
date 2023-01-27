package com.project.myacademy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    DUPLICATED_ACADEMY(HttpStatus.CONFLICT, "이미 존재하는 학원입니다."),
    DUPLICATED_EMPLOYEE(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    DUPLICATED_ACCOUNT(HttpStatus.CONFLICT, "이미 존재하는 계정명입니다."),
    DUPLICATED_STUDENT(HttpStatus.CONFLICT, "이미 존재하는 학생입니다."),
    DUPLICATED_PARENT(HttpStatus.CONFLICT, "이미 등록된 부모입니다."),
    DUPLICATED_LECTURE(HttpStatus.CONFLICT, "이미 존재하는 수업입니다."),
    DUPLICATED_TEACHER(HttpStatus.CONFLICT, "이미 존재하는 강사입니다."),
    DUPLICATED_PAYMENT(HttpStatus.CONFLICT, "이미 결제된 내역입니다."),
    DUPLICATED_ENROLLMENT(HttpStatus.CONFLICT, "이미 존재하는 수강 내역입니다."),
    OVER_REGISTRATION_NUMBER(HttpStatus.CONFLICT, "최대 수강정원을 초과했습니다."),
    DUPLICATED_WAITINGLIST(HttpStatus.CONFLICT, "이미 대기번호에 등록되어 있습니다."),


    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 계정명을 찾을 수 없습니다."),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 직원을 찾을 수 없습니다."),
    REQUEST_EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 직원을 해당 학원에서 찾을 수 없습니다."),
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학원을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 부모를 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수업을 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    UNIQUENESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 특이사항을 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강사를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제내역을 찾을 수 없습니다."),
    ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수강 이력을 찾을 수 없습니다."),
    WAITINGLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 대기번호를 찾을 수 없습니다."),
    TEACHER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강사 파일을 찾을 수 없습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 잘못되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "사용자가 권한이 없습니다."),

    NOT_ALLOWED_ROLE(HttpStatus.UNAUTHORIZED, "특정 권한의 회원만 접근할 수 있습니다."),
    NOT_ALLOWED_CHANGE(HttpStatus.UNAUTHORIZED, "ADMIN 계정을 변경하거나 삭제할 수 없습니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB에러가 발생했습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "요청이 이상합니다."),
    NOT_MATCH_OWNER(HttpStatus.BAD_REQUEST, "admin 계정 생성 시 학원 대표자 명과 일치해야 합니다."),
    BAD_DELETE_REQUEST(HttpStatus.BAD_REQUEST, "자신의 계정을 삭제할 수 없습니다."),
    BAD_CHANGE_REQUEST(HttpStatus.BAD_REQUEST, "자신의 계정 등급을 변경할 수 없습니다."),
    FILE_NOT_EXISTS(HttpStatus.BAD_REQUEST, "보낼 파일이 비어있습니다."),
    FILE_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "파일 업로드 용량을 초과했습니다."),
    WRONG_FILE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다"),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "결제 정보가 필요합니다.");

    private HttpStatus httpStatus;
    private String message;
}