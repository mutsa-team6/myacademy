package com.project.myacademy.domain.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRequest {
    //학생 이름
    @NotBlank(message = "학생 이름은 필수 입력 항목입니다.")
    private String name;
    //학생 학교
    @NotBlank(message = "학생 학교는 필수 입력 항목입니다.")
    private String school;
    //학생 전화번호
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "000-0000-0000 형식으로 전화번호를 입력해주세요.")
    private String phoneNum;
    //학생 이메일
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    //학생 생년월일
    @NotBlank(message = "학생 생년월일은 필수 입력 항목입니다.")
    private String birth;
    //부모 전화번호
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$",message = "000-0000-0000 형식으로 전화번호를 입력해주세요.")
    private String parentPhoneNum;

}
