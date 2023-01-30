package com.project.myacademy.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.myacademy.global.ErrorResponse;
import com.project.myacademy.global.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestControllerAdvice
@Slf4j
public class ExceptionManager {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> appExceptionHandler(AppException e) {
        log.error("AppException : {}",e.getErrorCode());
        ErrorResponse errorResponse = new ErrorResponse(e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(Response.error("ERROR", errorResponse));
    }

    /**
     * 파일 업로드 용량 초과시 발생
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.info("handleMaxUploadSizeExceededException", e);
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FILE_SIZE_EXCEED);
        return ResponseEntity.status(errorResponse.getErrorCode().getHttpStatus())
                .body(Response.error("ERROR", errorResponse));
    }


    /**
     * Security Chain 에서 발생하는 에러 응답 구성
     */
    public static void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {


        // 에러 응답코드 설정
        response.setStatus(errorCode.getHttpStatus().value());

        // 응답 body type JSON 타입으로 설정
        response.setContentType("application/json;charset=UTF-8");


        Response<ErrorDto> error = Response.error("ERROR",new ErrorDto(errorCode.toString(), errorCode.getMessage()));

        //예외 발생 시 Error 내용을 JSON화 한 후 응답 body에 담아서 보낸다.
        ObjectMapper obj = new ObjectMapper();
        String responseBody = obj.writeValueAsString(error);
        response.getWriter().write(responseBody);
    }
}