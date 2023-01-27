package com.project.myacademy.domain.uniqueness.dto;

import com.project.myacademy.domain.uniqueness.Uniqueness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReadAllUniquenessResponse {

    //특이사항 아이디
    private Long uniquenessId;
    //특이사항 내용
    private String body;

    public static ReadAllUniquenessResponse of(Uniqueness uniqueness) {
        return ReadAllUniquenessResponse.builder()
                .uniquenessId(uniqueness.getId())
                .body(uniqueness.getBody())
                .build();
    }
}
