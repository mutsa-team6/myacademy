package com.project.myacademy.domain.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateEmailRequest {

    private String email;
    private String title;
    private String body;
}
