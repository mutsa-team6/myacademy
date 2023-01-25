package com.project.myacademy.domain.waitinglist.dto;

import com.project.myacademy.domain.waitinglist.Waitinglist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ReadAllWaitinglistResponse {

    private Long studentId;
    private Long lectureId;

    public static ReadAllWaitinglistResponse of(Waitinglist waitinglist) {
        return ReadAllWaitinglistResponse.builder()
                .studentId(waitinglist.getStudent().getId())
                .lectureId(waitinglist.getLecture().getId())
                .build();
    }
}