package com.project.myacademy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    DUPLICATED_ACADEMY_NAME(HttpStatus.CONFLICT, "이미 존재하는 학원입니다."),
    DUPLICATED_EMPLOYEE_NAME(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    DUPLICATED_STUDENT_NAME(HttpStatus.CONFLICT, "이미 존재하는 학생입니다."),
    DUPLICATED_PARENT_NAME(HttpStatus.CONFLICT, "이미 등록된 부모입니다."),
    DUPLICATED_LECTURE_NAME(HttpStatus.CONFLICT, "이미 존재하는 수업입니다."),
    DUPLICATED_TEACHER_NAME(HttpStatus.CONFLICT, "이미 존재하는 강사입니다."),
    DUPLICATED_PAYMENT_NAME(HttpStatus.CONFLICT, "이미 결제된 내역입니다."),

    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학원을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 부모를 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수업을 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강사를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제내역을 찾을 수 없습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 잘못되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "사용자가 권한이 없습니다."),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DB에러"),
    INVALID_VALUE(HttpStatus.BAD_REQUEST, "요청이 이상합니다."),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "결제 정보가 필요합니다.");

    private HttpStatus httpStatus;
    private String message;
}