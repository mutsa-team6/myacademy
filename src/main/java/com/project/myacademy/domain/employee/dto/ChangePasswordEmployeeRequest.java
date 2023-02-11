package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class ChangePasswordEmployeeRequest {
    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    private String oldPassword;

    @NotBlank(message = "변경할 비밀번호는 필수 입력 항목입니다.")
    @Length(min=8,message = "비밀번호는 길이는 최소 8자리 입니다.")
    private String newPassword;
}
