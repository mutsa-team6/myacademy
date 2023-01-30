package com.project.myacademy.domain.lecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateLectureRequest {

    private String lectureName;
    private Integer lecturePrice;
    private Integer minimumCapacity;
    private Integer maximumCapacity;
    private String lectureDay;
    private String lectureTime;
    private LocalDate startDate;
    private LocalDate finishDate;
}
