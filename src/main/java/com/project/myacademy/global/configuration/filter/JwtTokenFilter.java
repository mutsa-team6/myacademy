package com.project.myacademy.global.configuration.filter;


import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.global.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

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

    private final EmployeeService employeeService;
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

        //계정에 맞는 권한 부여
        String employeeRole = employeeService.findRoleByAccount(account).name();
        log.info("employeeRole :{}", employeeRole);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(account, null, List.of(new SimpleGrantedAuthority(employeeRole)));

        log.info("{}",authenticationToken);

        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }
}
