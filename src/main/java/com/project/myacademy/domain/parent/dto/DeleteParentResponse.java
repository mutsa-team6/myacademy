package com.project.myacademy.domain.parent.dto;

import com.project.myacademy.domain.parent.Parent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class DeleteParentResponse {
    //부모 Id
    private Long id;
    //부모 이름
    private String name;

    public static DeleteParentResponse of(Parent parent) {
        return DeleteParentResponse.builder()
                .id(parent.getId())
                .name(parent.getName())
                .build();
    }
}
