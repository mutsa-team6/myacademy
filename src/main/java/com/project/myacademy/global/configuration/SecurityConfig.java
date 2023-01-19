package com.project.myacademy.global.configuration;

import com.project.myacademy.domain.employee.EmployeeService;
import com.project.myacademy.global.configuration.filter.JwtTokenFilter;
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
                .antMatchers("/api/v1/employees/signup", "/api/v1/employees/login", "/api/v1/employees/findaccount", "/swagger-ui/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasAnyRole("ADMIN","STAFF")
                .antMatchers(HttpMethod.PUT, "/api/v1/employees/changeRole/**").hasAnyRole("ADMIN","STAFF")
                .antMatchers(HttpMethod.GET, "/api/v1/employees").hasAnyRole("ADMIN")
                .and()

                .exceptionHandling()
                .accessDeniedHandler(customAccessDeniedHandler)

                .and()
                .addFilterBefore(new JwtTokenFilter(employeeService,secretKey), UsernamePasswordAuthenticationFilter.class)
                .build();

    }

}
