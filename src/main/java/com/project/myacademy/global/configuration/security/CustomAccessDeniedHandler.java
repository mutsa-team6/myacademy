package com.project.myacademy.global.configuration.security;

import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.exception.ExceptionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * "ADMIN" 혹은 "STAFF"만 접근할 수 있는 요청에 "ADMIN" 혹은 "STAFF"이 아닌 사용자가 요청할 시 예외 처리
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        log.info(" 에러 메세지 {}", accessDeniedException.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_PERMISSION;

        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('특정 권한의 회원만 접근할 수 있습니다.');  history.back();</script>");
        writer.flush();

        ExceptionManager.setErrorResponse(response, errorCode);

    }
}
