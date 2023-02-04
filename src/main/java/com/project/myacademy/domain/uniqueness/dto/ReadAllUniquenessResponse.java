package com.project.myacademy.domain.uniqueness.dto;

import com.project.myacademy.domain.uniqueness.Uniqueness;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Getter
@AllArgsConstructor
@Builder
public class ReadAllUniquenessResponse {

    //특이사항 아이디
    private Long uniquenessId;
    //특이사항 내용
    private String body;
    private String createdAt;
    private String author;

    public static ReadAllUniquenessResponse of(Uniqueness uniqueness) {
        return ReadAllUniquenessResponse.builder()
                .uniquenessId(uniqueness.getId())
                .body(uniqueness.getBody())
                .createdAt(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Timestamp.valueOf(uniqueness.getCreatedAt())))
                .author(uniqueness.getAuthor())
                .build();
    }

}
