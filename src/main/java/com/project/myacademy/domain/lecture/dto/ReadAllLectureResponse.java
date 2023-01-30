package com.project.myacademy.domain.lecture.dto;

import com.project.myacademy.domain.lecture.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ReadAllLectureResponse {

    private Long lectureId;
    private String lectureName;
    private String teacherName;
    private Integer lecturePrice;
    private Integer maximumCapacity;
    private String lectureDay;
    private String lectureTime;
    private LocalDate startDate;
    private LocalDate finishDate;
    private Integer currentEnrollmentNumber;

    public static ReadAllLectureResponse of(Lecture lecture) {
        return ReadAllLectureResponse.builder()
                .lectureId(lecture.getId())
                .lectureName(lecture.getName())
                .teacherName(lecture.getEmployee().getName())
                .lecturePrice(lecture.getPrice())
                .maximumCapacity(lecture.getMaximumCapacity())
                .lectureDay(lecture.getLectureDay())
                .lectureTime(lecture.getLectureTime())
                .startDate(lecture.getStartDate())
                .finishDate(lecture.getFinishDate())
                .currentEnrollmentNumber(lecture.getCurrentEnrollmentNumber())
                .build();
    }
}