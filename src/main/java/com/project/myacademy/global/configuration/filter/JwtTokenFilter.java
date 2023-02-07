package com.project.myacademy.global.configuration.filter;


import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.global.configuration.refreshToken.RefreshToken;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import com.project.myacademy.global.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final EmployeeRepository employeeRepository;

    private final RefreshTokenRepository refreshTokenRepository;
    private final String secretKey;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        Cookie[] list = request.getCookies();
        
        //쿠키가 없는 경우 그냥 진행
        try {
            for (Cookie cookie : list) {
                if (cookie.getName().equals("token")) {
                    token = cookie.getValue();
                }
            }
        }catch(Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token == null || token.equals("deleted")) {
            filterChain.doFilter(request, response);
            return;
        }

        Employee found;

        // Access Token 유효성 검증
        try {
            JwtTokenUtil.isExpired(token, secretKey);

            found = employeeRepository.findByEmail(JwtTokenUtil.getEmail(token, secretKey))
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

            //Access Token이 유효하지 않는다면 아래 로직을 지나갈 것
        } catch (ExpiredJwtException e) {
            log.error("💡 Access Token 이 만료되었습니다.");

            // redis에 저장되어있는 토큰 정보를 만료된 access token으로 찾아온다.
            RefreshToken foundTokenInfo = refreshTokenRepository.findByAccessToken(token)
                    .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));

            String refreshToken = foundTokenInfo.getRefreshToken();

            // 만약 refresh 토큰도 만료되었다면, ExceptionHandlerFilter에서 처리된다.
            JwtTokenUtil.isExpired(refreshToken, secretKey);

            // refresh 토큰이 아직 유효하다면, redis에 함께 저장해둔, employeeId를 가져온다.
            Long employeeId = Long.valueOf(foundTokenInfo.getId());

             found = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

             //위 사용자 정보로 다시 Access Token을 만들어 발급한다.
            token = JwtTokenUtil.createToken(found.getAccount(), found.getEmail(), secretKey);

            //새로 발급한 Access Token으로 Redis도 업데이트를 해준다.
            refreshTokenRepository.save(new RefreshToken(String.valueOf(employeeId), refreshToken, token));
            //클라이언트 측 쿠키의 Access Token도 업데이트를 해준다.
            CookieGenerator cookieGenerator = new CookieGenerator();
            cookieGenerator.setCookieName("token");
            cookieGenerator.setCookieHttpOnly(true);
            cookieGenerator.addCookie(response, token);
            cookieGenerator.setCookieMaxAge(60 * 60);//1시간
        }

        String employeeRole = found.getEmployeeRole().name();

        String employeeInfo = found.getAccount() + "@" + found.getAcademy().getId();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(employeeInfo, null, List.of(new SimpleGrantedAuthority(employeeRole)));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
