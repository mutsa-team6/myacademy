package com.project.myacademy.domain.parent.dto;

import com.project.myacademy.domain.parent.Parent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
public class UpdateParentResponse {
    //부모 Id
    private Long id;
    //부모 이름
    private String name;
    //부모 핸드폰번호
    private String phoneNum;
    //부모 주소
    private String address;
    //부모 정보 마지막 수정 일시
    private LocalDateTime lastModifiedAt;

    public static UpdateParentResponse of(Parent parent) {
        return UpdateParentResponse.builder()
                .id(parent.getId())
                .name(parent.getName())
                .phoneNum(parent.getPhoneNum())
                .address(parent.getAddress())
                .lastModifiedAt(parent.getLastModifiedAt())
                .build();
    }
}
