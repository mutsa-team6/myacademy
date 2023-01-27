package com.project.myacademy.global.configuration.filter;

import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.exception.ExceptionManager;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    /**
     * 토큰 관련 에러 핸들링
     * JwtTokenFilter 에서 발생하는 에러를 핸들링해준다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {

            //토큰의 유효기간 만료
            log.error("만료된 토큰입니다");
            sendMessage(response);

        } catch (JwtException | IllegalArgumentException e) {
            //유효하지 않은 토큰
            log.error("유효하지 않은 토큰이 입력되었습니다.");
            sendMessage(response);


        }catch (ArrayIndexOutOfBoundsException e) {

            log.error("토큰을 추출할 수 없습니다.");
            sendMessage(response);


        } catch (NullPointerException e) {

            filterChain.doFilter(request, response);
            sendMessage(response);

        }
    }

    void sendMessage(HttpServletResponse response) throws IOException {

        CookieGenerator cookieGenerator = new CookieGenerator();
        cookieGenerator.setCookieName("token");
        cookieGenerator.addCookie(response, "deleted");
        cookieGenerator.setCookieMaxAge(0);

        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.println("<script>alert('토큰이 만료되어 토큰을 삭제합니다.');  location.reload();</script>");
        writer.flush();
    }
}
