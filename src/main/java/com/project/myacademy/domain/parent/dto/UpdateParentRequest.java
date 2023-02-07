package com.project.myacademy.domain.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateParentRequest {
    //부모 이름
    private String name;
    //부모 핸드폰번호
    private String phoneNum;
    //부모 주소
    private String address;
}
