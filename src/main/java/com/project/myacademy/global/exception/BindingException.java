package com.project.myacademy.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BindingException extends RuntimeException{

    private ErrorCode errorCode;
    private String message;

}