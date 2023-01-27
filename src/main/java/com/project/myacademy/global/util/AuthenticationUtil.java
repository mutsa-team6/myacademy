package com.project.myacademy.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;

@Slf4j
public class AuthenticationUtil {

    public static String getAccountFromAuth(Authentication authentication) {
        String[] info = authentication.getName().split("@");
        return info[0];
    }

    public static Long getAcademyIdFromAuth(Authentication authentication) {
        String[] info = authentication.getName().split("@");
        return Long.valueOf(info[1]);
    }

}
