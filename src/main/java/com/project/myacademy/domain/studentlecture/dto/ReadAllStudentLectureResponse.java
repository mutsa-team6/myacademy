package com.project.myacademy.domain.studentlecture.dto;

import com.project.myacademy.domain.studentlecture.StudentLecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ReadAllStudentLectureResponse {

    private Long studentLectureId;
    private String lectureName;
    private Integer lecturePrice;
    private String studentName;
    private String memo;

    public static ReadAllStudentLectureResponse of(StudentLecture studentLecture) {
        return ReadAllStudentLectureResponse.builder()
                .studentLectureId(studentLecture.getId())
                .lectureName(studentLecture.getLecture().getName())
                .lecturePrice(studentLecture.getLecture().getPrice())
                .studentName(studentLecture.getStudent().getName())
                .memo(studentLecture.getMemo())
                .build();
    }
}
