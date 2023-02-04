package com.project.myacademy.global.configuration;

import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.global.configuration.filter.ExceptionHandlerFilter;
import com.project.myacademy.global.configuration.filter.JwtTokenFilter;
import com.project.myacademy.global.configuration.oauth.CustomOAuth2UserService;
import com.project.myacademy.global.configuration.oauth.Oauth2FailureHandler;
import com.project.myacademy.global.configuration.oauth.Oauth2SuccessHandler;
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
    private final EmployeeService employeeService;

    private final Oauth2SuccessHandler oauth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;

    private final CustomOAuth2UserService customOAuth2UserService;

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
                .antMatchers("/api/v1/academies/**/employees/signup", "/api/v1/academies/**/employees/login", "/api/v1/employees/findaccount", "/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/v1/academies/**/employees/**").hasAnyRole("ADMIN","STAFF")
                .antMatchers(HttpMethod.PUT, "/api/v1/academies/**/changeRole/**").hasAnyRole("ADMIN","STAFF")
                .antMatchers(HttpMethod.GET, "/api/v1/academies/**/employees").hasAnyRole("ADMIN")
                .antMatchers("/academy/employees").hasRole("ADMIN")
                .antMatchers( "/academy/students/list").hasAnyRole("ADMIN","STAFF")
                .antMatchers("api/v1/academies/**").authenticated()
                .antMatchers("/academy/**").authenticated()
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
                .addFilterBefore(new JwtTokenFilter(employeeService,secretKey), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ExceptionHandlerFilter(), JwtTokenFilter.class)
                .build();

    }

}
