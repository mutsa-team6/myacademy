package com.project.myacademy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    DUPLICATED_ACADEMY(HttpStatus.CONFLICT, "이미 존재하는 학원입니다."),
    DUPLICATED_EMPLOYEE(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    DUPLICATED_STUDENT(HttpStatus.CONFLICT, "이미 존재하는 학생입니다."),
    DUPLICATED_PARENT(HttpStatus.CONFLICT, "이미 등록된 부모입니다."),
    DUPLICATED_LECTURE(HttpStatus.CONFLICT, "이미 존재하는 수업입니다."),
    DUPLICATED_TEACHER(HttpStatus.CONFLICT, "이미 존재하는 강사입니다."),
    DUPLICATED_PAYMENT(HttpStatus.CONFLICT, "이미 결제된 내역입니다."),

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 계정을 찾을 수 없습니다."),
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학원을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 부모를 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수업을 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    UNIQUENESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 특이사항을 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강사를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제내역을 찾을 수 없습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 잘못되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "사용자가 권한이 없습니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB에러가 발생했습니다."),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "요청이 이상합니다."),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "결제 정보가 필요합니다.");

    private HttpStatus httpStatus;
    private String message;
}