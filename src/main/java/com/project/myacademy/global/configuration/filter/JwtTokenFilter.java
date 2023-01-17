package com.project.myacademy.global.configuration.filter;


import com.project.myacademy.global.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

//    private final EmployeeService employeeService;
    private final String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //UserName Token에서 꺼내기
        final String authorization = request.getHeader(AUTHORIZATION);

        //조건 -> 올바른 형식이 아니라면 권한을 부여하지 않음
        if(authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //Token 분리
        final String token;
        try {
            token = authorization.split(" ")[1].trim(); // 1번에는 토큰내용
        } catch(Exception e) { // 예외발생 -> Filter종료
            log.error("Token 추출에 실패했습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        //토큰만료 check
        if (JwtTokenUtil.isExpired(token, secretKey)) {
            log.error("Token 이 만료되었습니다.");
            filterChain.doFilter(request, response);
            return;
        }

        //userName 분리
        // Claims은 Object 타입으로 들어가는데 꺼낼 때는 String 타입으로 저장해야 한다.
        String account = JwtTokenUtil.getAccount(token,secretKey);
        log.info("account:{}", account);


//        String employeeRole = employeeService.findRoleByAccount(account).name();
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority(employeeRole)));

        //권한부여
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(account, null, List.of(new SimpleGrantedAuthority("USER")));

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
