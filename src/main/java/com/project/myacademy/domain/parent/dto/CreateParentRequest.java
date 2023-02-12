package com.project.myacademy.domain.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateParentRequest {
    //부모 이름
    @NotBlank(message = "부모 이름은 필수 입력 항목입니다.")
    private String name;
    //부모 핸드폰번호
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "000-0000-0000 형식으로 전화번호를 입력해주세요.")
    private String phoneNum;
    //부모 주소
    @NotBlank(message = "부모 주소는 필수 입력 항목입니다.")
    private String address;
}
