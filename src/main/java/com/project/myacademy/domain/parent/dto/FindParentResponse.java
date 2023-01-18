package com.project.myacademy.domain.parent.dto;

import com.project.myacademy.domain.parent.Parent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FindParentResponse {
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
    //부모 정보 등록 일시
    private LocalDateTime createAt;
    //부모 정보 마지막 수정 일시
    private LocalDateTime lastModifiedAt;

    public static FindParentResponse of(Parent parent) {
        return FindParentResponse.builder()
                .id(parent.getId())
                .name(parent.getName())
                .parentRecognizedCode(parent.getParentRecognizedCode())
                .phoneNum(parent.getPhoneNum())
                .address(parent.getAddress())
                .createAt(parent.getCreatedAt())
                .lastModifiedAt(parent.getLastModifiedAt())
                .build();
    }
}
