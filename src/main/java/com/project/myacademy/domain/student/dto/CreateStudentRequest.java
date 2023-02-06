package com.project.myacademy.domain.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRequest {

    //학생 이름
    private String name;
    //학생 학교
    private String school;
    //학생 전화번호
    private String phoneNum;
    //학생 이메일
    private String email;
    //학생 생년월일
    private String birth;
    //부모 전화번호
    private String parentPhoneNum;

}
