package com.project.myacademy.domain.lecture.dto;

import com.project.myacademy.domain.enrollment.dto.FindStudentInfoFromEnrollmentByLectureResponse;
import com.project.myacademy.domain.lecture.Lecture;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
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
    private long completePaymentNumber;
    private Long waitingNum;
    private List<FindStudentInfoFromEnrollmentByLectureResponse> registeredStudent = new ArrayList<>();
    private List<FindStudentInfoFromEnrollmentByLectureResponse> waitingStudent = new ArrayList<>();

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