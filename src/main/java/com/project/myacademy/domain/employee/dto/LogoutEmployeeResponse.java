package com.project.myacademy.domain.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class LogoutEmployeeResponse {
    private String message = "로그아웃되었습니다";
}
