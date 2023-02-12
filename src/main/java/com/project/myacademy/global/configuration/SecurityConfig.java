package com.project.myacademy.global.configuration;

import com.project.myacademy.domain.employee.EmployeeRepository;
import com.project.myacademy.global.configuration.filter.ExceptionHandlerFilter;
import com.project.myacademy.global.configuration.filter.JwtTokenFilter;
import com.project.myacademy.global.configuration.oauth.CustomOAuth2UserService;
import com.project.myacademy.global.configuration.oauth.Oauth2FailureHandler;
import com.project.myacademy.global.configuration.oauth.Oauth2SuccessHandler;
import com.project.myacademy.global.configuration.refreshToken.RefreshTokenRepository;
import com.project.myacademy.global.configuration.security.CustomAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${jwt.token.secret}")
    private String secretKey;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

    protected static final String[] PERMIT_ALL_CONTROLLER_URL = {"/", "/join", "/login", "/find/account", "/find/password", "/academies", "/swagger-ui/**"};
    protected static final String[] REFUSED_USER_CONTROLLER_URL
            = {"/academy/announcements/write",
            "/academy/announcements/edit/**",
            "/academy/student/register",
            "/academy/lecture/register/**",
            "/academy/enrollment/**",
            "/academy/payment/**",
            "/academy/discount"
    };
    protected static final String[] ONLY_ADMIN_CONTROLLER_URL = {"/academy/employees"};


    protected static final String[] PERMIT_ALL_API_URL_GET = {"/api/v1/academies/employees/logout"
    };
    protected static final String[] PERMIT_ALL_API_URL_POST = {
            "/api/v1/academies"
            , "/api/v1/academies/**/employees/signup"
            , "/api/v1/academies/**/employees/login"
            , "/api/v1/academies/employee/findAccount"};
    protected static final String[] PERMIT_ALL_API_URL_PUT = {"/api/v1/academies/employee/findPassword"
    };

    protected static final String[] AUTH_API_URL_GET = {
            "/api/v1/academies/**/my"
            , "/api/v1/academies/**/employees/**/files/download"
            , "/api/v1/academies/**/announcements/**"
            , "/api/v1/academies/**/announcements"
            , "/api/v1/academies/**/announcements/type/**"
            , "/api/v1/academies/**/announcements/**/files/download"
            , "/api/v1/academies/**/parents/**"
            , "/api/v1/academies/**/students/**"
            , "/api/v1/academies/**/students"
            , "/api/v1/academies/**/students/**/uniqueness"
            , "/api/v1/academies/**/lectures"
            , "/api/v1/academies/**/enrollments"
            , "/api/v1/academies/**/waitinglists"
            , "/api/v1/academies/**/discounts"
            , "/api/v1/academies/**/enrollments/**/discounts"
            , "/api/v1/payments/success"
            , "/api/v1/payments/fail"};
    protected static final String[] AUTH_API_URL_POST = {
            "/api/v1/academies/**/employee/changePassword"
            , "/api/v1/academies/**/employees/**/files/upload"
            , "/api/v1/academies/**/students/**/uniqueness"
    };
    protected static final String[] AUTH_API_URL_PUT = {"/api/v1/academies/**"};


    protected static final String[] REFUSED_USER_API_URL_POST = {
            "/api/v1/academies/**/announcements"
            , "/api/v1/academies/**/announcements/**/files/upload"
            , "/api/v1/academies/**/parents"
            , "/api/v1/academies/**/students"
            , "/api/v1/academies/**/employees/**/lectures"
            , "/api/v1/academies/**/students/**/lectures/**/enrollments/**"
            , "/api/v1/academies/**/students/**/lectures/**/enrollments"
            , "/api/v1/academies/**/students/**/lectures/**/waitinglists"
            , "/api/v1/academies/**/discounts"
            , "/api/v1/academies/**/discounts/check"
            , "/api/v1/payments/students/**"
            , "/api/v1/payments/cancel"
    };
    protected static final String[] REFUSED_USER_API_URL_PUT = {
            "/api/v1/academies/**/announcements/**"
            , "/api/v1/academies/**/parents/**"
            , "/api/v1/academies/**/students/**"
            , "/api/v1/academies/**/students/**/uniqueness/**"
            , "/api/v1/academies/**/lectures/**"
            , "/api/v1/academies/**/students/**/lectures/**/enrollments/**"

    };
    protected static final String[] REFUSED_USER_API_URL_DELETE = {
            "/api/v1/academies/**/employees/**/employeeProfiles/**/files"
            , "/api/v1/academies/**/announcements/**"
            , "/api/v1/academies/**/announcements/**/announcementFiles/**/files"
            , "/api/v1/academies/**/parents/**"
            , "/api/v1/academies/**/students/**"
            , "/api/v1/academies/**/lectures/**"
            , "/api/v1/academies/**/students/**/lectures/**/waitinglists/**"
            , "/api/v1/academies/**/discounts/**"
    };


    protected static final String[] ONLY_ADMIN_API_URL_POST = {"/api/v1/academies/**/files/upload"};

    protected static final String[] AUTH_API_URL_DELETE = {"/api/v1/academies/**/students/**/uniqueness/**"};
    protected static final String[] ONLY_ADMIN_API_URL_DELETE = {
            "/api/v1/academies/**/academyProfiles/**/files"
            , "/api/v1/academies/**/employees/**"
            };
    protected static final String[] ONLY_ADMIN_API_URL_GET = {
            "/api/v1/academies/**/files/upload"
            , "/api/v1/academies/**/employees"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()


                .authorizeRequests()
                .antMatchers(PERMIT_ALL_CONTROLLER_URL).permitAll()
                .antMatchers(ONLY_ADMIN_CONTROLLER_URL).hasRole("ADMIN")
                .antMatchers(REFUSED_USER_CONTROLLER_URL).hasAnyRole("ADMIN", "STAFF")
                .antMatchers("/academy/**").authenticated()

                .antMatchers(HttpMethod.GET, PERMIT_ALL_API_URL_GET).permitAll()
                .antMatchers(HttpMethod.POST, PERMIT_ALL_API_URL_POST).permitAll()
                .antMatchers(HttpMethod.PUT, PERMIT_ALL_API_URL_PUT).permitAll()

                .antMatchers(HttpMethod.GET, AUTH_API_URL_GET).authenticated()
                .antMatchers(HttpMethod.POST, AUTH_API_URL_POST).authenticated()
                .antMatchers(HttpMethod.PUT, AUTH_API_URL_PUT).authenticated()
                .antMatchers(HttpMethod.DELETE, AUTH_API_URL_DELETE).authenticated()

                .antMatchers(HttpMethod.POST, REFUSED_USER_API_URL_POST).hasAnyRole("ADMIN", "STAFF")
                .antMatchers(HttpMethod.PUT, REFUSED_USER_API_URL_PUT).hasAnyRole("ADMIN", "STAFF")
                .antMatchers(HttpMethod.DELETE, REFUSED_USER_API_URL_DELETE).hasAnyRole("ADMIN", "STAFF")

                .antMatchers(HttpMethod.GET, ONLY_ADMIN_API_URL_GET).hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, ONLY_ADMIN_API_URL_POST).hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, ONLY_ADMIN_API_URL_DELETE).hasRole("ADMIN")

                .and()

                .oauth2Login().loginPage("/login")
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .userService(customOAuth2UserService)
                .and()
                .successHandler(oauth2SuccessHandler)
                .failureHandler(oauth2FailureHandler)
                .and()

                .exceptionHandling()
                .accessDeniedHandler(customAccessDeniedHandler)

                .and()
                .addFilterBefore(new JwtTokenFilter(employeeRepository, refreshTokenRepository, secretKey), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ExceptionHandlerFilter(), JwtTokenFilter.class)
                .build();

    }

}
