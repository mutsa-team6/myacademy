package com.project.myacademy.global;

import com.project.myacademy.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private ErrorCode errorCode;
    private String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.message = errorCode.getMessage();
    }
}

