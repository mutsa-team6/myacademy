package com.project.myacademy.global.configuration.oauth;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum OAuthAttributes {

    GOOGLE("google", (attributes) -> {
        return new EmployeeProfile(
                (String) attributes.get("name"),
                (String) attributes.get("email")
        );
    }),
    NAVER("naver", (attributes) -> {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return new EmployeeProfile(
                (String) response.get("name"),
                (String) response.get("email")
        );
    });

    private final String registrationId;
    private final Function<Map<String, Object>, EmployeeProfile> of;

    OAuthAttributes(String registrationId, Function<Map<String, Object>, EmployeeProfile> of) {
        this.registrationId = registrationId;
        this.of = of;
    }

    public static EmployeeProfile extract(String registrationId, Map<String, Object> attributes) {
        return Arrays.stream(values())
                .filter(provider -> registrationId.equals(provider.registrationId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new)
                .of.apply(attributes);
    }
}