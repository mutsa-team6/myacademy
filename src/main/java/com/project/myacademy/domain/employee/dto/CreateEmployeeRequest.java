package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
public class CreateEmployeeRequest {
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;
    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "000-0000-0000 형식으로 전화번호를 입력해주세요.")
    private String phoneNum;
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    @NotBlank(message = "계정명은 필수 입력 항목입니다.")
    private String account;
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",message = "최소 8글자로 입력해주시고, 글자 1개, 숫자 1개, 특수문자 1개를 포함해주세요.")
    private String password;
    private String employeeType;
    private String subject;

}
