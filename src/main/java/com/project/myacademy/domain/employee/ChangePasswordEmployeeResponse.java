package com.project.myacademy.domain.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangePasswordEmployeeResponse {
    //비밀번호가 변경된 사람의 계정
    private String account;
    //변경되었다는 내용을 담을 메시지
    private String message;
}
