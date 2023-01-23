package com.project.myacademy.global.configuration.oauth;


import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.global.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.token.secret}")
    private String key;

    private final EmployeeRepository employeeRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();

        // 구글 인증시 아래 로직
        String realName = (String)oAuth2User.getAttribute("name");
        String email = (String)oAuth2User.getAttribute("email");
        log.info("🌈 구글 인증 시 이름 추출 [{}] || 이메일 추출 [{}]",realName,email);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 네이버 인증 시 아래 로직
        if(attributes.get("sub")==null){
            Map<String, Object> response2 =(Map<String, Object>) attributes.get("response");
            realName = (String) response2.get("name");
            email = (String) response2.get("email");
            log.info("🌈 네이버 인증 시 이름 추출 [{}] || 이메일 추출 [{}]",realName,email);
        }

        // 이름과 이메일이 둘다 일치하는 회원이 저장되어있을 것
        Employee foundEmployee = employeeRepository.findByNameAndEmail(realName, email).get();
        String foundAccount = foundEmployee.getAccount();
        log.info("🌈 소셜 로그인 인증한 계정명 [{}]",foundAccount);


        // 회원 계정으로 토큰 생성 후 쿼리 파라미터로 보냄
        String token = JwtTokenUtil.createToken(foundAccount,key,1000*60*60);

        response.sendRedirect("/oauth2/redirect"+"?token="+token);

    }
}
