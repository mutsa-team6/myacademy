package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateEmployeeRequest {
    @NotBlank(message = "주소를 공백으로 바꿀 수 없습니다.")
    private String address;

    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "000-0000-0000 형식으로 전화번호를 입력해주세요.")
    private String phoneNum;

    @NotBlank(message = "과목명을 공백으로 바꿀 수 없습니다.")
    private String subject;
}
