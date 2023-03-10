package com.project.myacademy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    DUPLICATED_ACADEMY(HttpStatus.CONFLICT, "이미 등록되어 있는 학원입니다."),
    DUPLICATED_BUSINESS_REGISTRATION_NUMBER(HttpStatus.CONFLICT, "이미 등록된 사업자 등록번호입니다."),
    DUPLICATED_ACCOUNT(HttpStatus.CONFLICT, "이미 등록되어 있는 계정명입니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 등록되어 있는 이메일입니다."),
    DUPLICATED_PHONENUM(HttpStatus.CONFLICT,"이미 등록되어 있는 핸드폰번호입니다." ),
    DUPLICATED_STUDENT(HttpStatus.CONFLICT, "이미 등록되어 있는 학생입니다."),
    DUPLICATED_PARENT(HttpStatus.CONFLICT, "이미 등록되어 있는 부모입니다."),
    DUPLICATED_LECTURE(HttpStatus.CONFLICT, "이미 등록되어 있는 강좌입니다."),
    DUPLICATED_PAYMENT(HttpStatus.CONFLICT, "이미 결제된 내역입니다."),
    DUPLICATED_ENROLLMENT(HttpStatus.CONFLICT, "이미 중복된 수강신청 입니다."),
    OVER_REGISTRATION_NUMBER(HttpStatus.CONFLICT, "최대 수강정원을 초과했습니다."),
    DUPLICATED_WAITINGLIST(HttpStatus.CONFLICT, "이미 대기번호에 등록되어 있습니다."),
    DUPLICATED_DISCOUNT(HttpStatus.CONFLICT, "이미 등록되어 있는 할인 정책입니다."),
    NOT_TEACHER(HttpStatus.CONFLICT,"해당 사용자는 강사가 아닙니다."),

    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일로 가입된 사용자를 찾을 수 없습니다."),
    REQUEST_EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 사용자를 해당 학원에서 찾을 수 없습니다."),
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학원을 찾을 수 없습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 부모를 찾을 수 없습니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강좌를 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학생을 찾을 수 없습니다."),
    UNIQUENESS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 특이사항을 찾을 수 없습니다."),
    TEACHER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 강사를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제내역을 찾을 수 없습니다."),
    ANNOUNCEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항을 찾을 수 없습니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수강신청 내역을 찾을 수 없습니다."),
    WAITINGLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 수강신청 대기 번호를 찾을 수 없습니다."),
    ACADEMY_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 학원 이미지 파일을 찾을 수 없습니다."),
    EMPLOYEE_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 이미지 파일을 찾을 수 없습니다."),
    ANNOUNCEMENT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공지사항 파일을 찾을 수 없습니다."),
    DISCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 할인정책을 찾을 수 없습니다."),
    CANCLE_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 결제 취소 내역을 찾을 수 없습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 일치하지 않습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "특정 권한의 회원만 접근할 수 있습니다."),
    NOT_ALLOWED_CHANGE(HttpStatus.UNAUTHORIZED, "ADMIN 계정을 변경하거나 삭제할 수 없습니다."),

    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    NOT_MATCH_OWNER(HttpStatus.BAD_REQUEST, "admin 계정 생성 시, 사용자의 실명과 학원 대표자 명이 일치해야 합니다."),
    BAD_DELETE_REQUEST(HttpStatus.BAD_REQUEST, "자신의 계정을 삭제할 수 없습니다."),
    BAD_CHANGE_REQUEST(HttpStatus.BAD_REQUEST, "자신의 계정 등급을 변경할 수 없습니다."),
    FILE_NOT_EXISTS(HttpStatus.BAD_REQUEST, "첨부된 파일이 존재하지 않습니다."),
    FILE_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "파일 업로드 용량을 초과했습니다."),
    WRONG_FILE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일입니다"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "기존 비밀번호와 동일한 비밀번호로는 변경 할 수 없습니다."),
    CANNOT_REGISTER_WAITINGLIST(HttpStatus.BAD_REQUEST, "아직 수강 정원이 다 차지 않아 수강 등록으로 진행해야 합니다."),


    BINDING_ERROR(HttpStatus.FORBIDDEN,"유효하지 않은 Request Body 필드가 있습니다."),
    EMPTY_SUBJECT_FORBIDDEN(HttpStatus.FORBIDDEN,"원장 · 강사 로 회원가입 시 담당 과목명을 입력해주세요."),
    EMPTY_EMPLOYEE_TYPE(HttpStatus.FORBIDDEN,"회원가입 시 사용자 유형 선택은 필수 입니다."),
    PAYMENT_ERROR_ORDER_PRICE(HttpStatus.FORBIDDEN,"요청하신 가격이 일치하지 않습니다."),
    PAYMENT_ERROR_ORDER_PAY_TYPE(HttpStatus.FORBIDDEN,"요청하신 지불 방식이 일치하지 않습니다."),
    PAYMENT_ERROR_ORDER_NAME(HttpStatus.FORBIDDEN,"요청하신 주문 이름이 일치하지 않습니다."),
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "결제 정보가 필요합니다.");


    private HttpStatus httpStatus;
    private String message;
}