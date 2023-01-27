package com.project.myacademy.domain.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateParentRequest {
    //부모 이름
    private String name;
    //부모 유형
    private Integer parentRecognizedCode;
    //부모 핸드폰번호
    private String phoneNum;
    //부모 주소
    private String address;
}
