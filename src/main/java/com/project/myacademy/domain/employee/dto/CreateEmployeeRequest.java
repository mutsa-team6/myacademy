package com.project.myacademy.domain.employee.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
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
    @Length(min=8,message = "비밀번호는 길이는 최소 8자리 입니다.")
    private String password;
    @NotBlank(message = "직원 유형은 필수 입력 항목입니다.")
    private String employeeType;
    @NotBlank(message = "과목명 필수 입력 항목입니다.")
    private String subject;

}
