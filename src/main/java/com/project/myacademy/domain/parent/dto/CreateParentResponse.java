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
    //등록된 학원 id
    private Long academyId;
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

    public static CreateParentResponse of(Parent parent) {
        return CreateParentResponse.builder()
                .academyId(parent.getAcademyId())
                .id(parent.getId())
                .name(parent.getName())
                .parentRecognizedCode(parent.getParentRecognizedCode())
                .phoneNum(parent.getPhoneNum())
                .address(parent.getAddress())
                .build();
    }
}
