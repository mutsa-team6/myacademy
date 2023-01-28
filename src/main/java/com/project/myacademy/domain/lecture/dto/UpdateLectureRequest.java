package com.project.myacademy.domain.lecture.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateLectureRequest {

    private String lectureName;
    private Integer lecturePrice;
//    private String teacherName;
    private Integer minimumCapacity;
    private Integer maximumCapacity;
    private String lectureDay;
    private String lectureTime;
    private LocalDate startDate;
    private LocalDate finishDate;
}