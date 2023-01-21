package com.project.myacademy.global.configuration.oauth;

import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.global.exception.AppException;
import com.project.myacademy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final EmployeeRepository employeeRepository;
    private final HttpSession httpSession;

    @Value("${jwt.token.secret}")
    private String key;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest,OAuth2User> delegate =
                new DefaultOAuth2UserService();

        // 소셜에서 인증받아서 가져온 유저 정보를 담고 있다.
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 어떤 서비스인지(구글, 네이버 등등) -> 로그 찍어보면 naver 혹은 google 이 나온다.
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();

        log.info("🌈 소셜 인증한 서비스 [{}]",registrationId);


        // OAuth2 로그인 진행 시 키가 되는 필드 값, 구글은 sub 네이버는 response 라는 이름을 갖는다.
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        //서비스 마다 다른 키와 벨류 값을 변환하여 객체를 만든다.
        EmployeeProfile employeeProfile = OAuthAttributes.extract(registrationId, attributes);

        // 소셜 로그인으로 들어온 이메일과 같은 이메일로 가입된 회원의 실명 + 이메일이 동일한 경우 승인
        Employee employee = check(employeeProfile);


        // 로그인 성공하면 세션에 회원 실명 저장
        httpSession.setAttribute("name",employee.getName());

        // 해당 계정이 갖고 있는 권한 그대로 주입
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(employee.getEmployeeRole().name())),
                attributes,
                userNameAttributeName);

    }

    // 소셜 로그인 시, 소셜 로그인에 등록된 실명과 이메일로 가입한 회원이 존재하지 않을 경우 에러 처리
    private Employee check(EmployeeProfile employeeProfile) {
        Employee employee = employeeRepository.findByNameAndEmail(employeeProfile.getName(), employeeProfile.getEmail())
                .orElseThrow(() -> new OAuth2AuthenticationException("가입된 회원이 아닙니다."));

        return employee;
    }

}
