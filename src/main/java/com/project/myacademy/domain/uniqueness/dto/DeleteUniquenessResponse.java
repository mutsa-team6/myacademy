package com.project.myacademy.domain.uniqueness.dto;

import com.project.myacademy.domain.uniqueness.Uniqueness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DeleteUniquenessResponse {
    //학생 id
    private Long studentId;
    //학생 이름
    private String studentName;
    //특이사항 id
    private Long uniquenessId;
    //특이사항 내용
    private String body;

    public static DeleteUniquenessResponse of(Uniqueness uniqueness) {
        return DeleteUniquenessResponse.builder()
                .studentId(uniqueness.getStudent().getId())
                .studentName(uniqueness.getStudent().getName())
                .uniquenessId(uniqueness.getId())
                .body(uniqueness.getBody())
                .build();
    }
}
