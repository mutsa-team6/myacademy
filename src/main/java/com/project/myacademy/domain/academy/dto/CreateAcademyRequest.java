package com.project.myacademy.domain.academy.dto;

import com.project.myacademy.domain.academy.Academy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateAcademyRequest {

    @NotBlank(message = "학원 이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "학원 주소는 필수 입력 항목입니다.")
    private String address;
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",message = "(2~3자리)-(3~4자리)-(4자리) 형식으로 전화번호를 입력해주세요.")
    private String phoneNum;
    @NotBlank(message = "학원 대표자명은 필수 입력 항목입니다.")
    private String owner;
    @Pattern(regexp = "\\d{10}",message = "사업자 등록번호는 숫자만 10자리로 입력해주세요.")
    private String businessRegistrationNumber;
}
