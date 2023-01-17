package com.project.myacademy.domain.parent.dto;

import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.student.dto.CreateStudentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CreateParentResponse {
    //부모 Id
    private Long id;
    //부모 이름
    private String name;
    //부모 유형
    private Integer parentRecognizedCode;
    //부모 핸드폰번호
    private String phoneNum;
    //부모 주소
    private String address;

    public static CreateParentResponse of(Parent savedParent) {
        return CreateParentResponse.builder()
                .id(savedParent.getId())
                .name(savedParent.getName())
                .parentRecognizedCode(savedParent.getParentRecognizedCode())
                .phoneNum(savedParent.getPhoneNum())
                .address(savedParent.getAddress())
                .build();
    }
}
