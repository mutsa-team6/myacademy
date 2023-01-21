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
public class CreateLectureRequest {

    private String lectureName;
    private Integer lecturePrice;
//    private String teacherName;
    private Integer minimumCapacity;
    private Integer maximumCapacity;
    private String lectureDay;
    private String lectureTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate finishDate;
}
