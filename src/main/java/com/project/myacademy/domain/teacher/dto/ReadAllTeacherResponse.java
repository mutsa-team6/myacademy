package com.project.myacademy.domain.teacher.dto;

import com.project.myacademy.domain.teacher.Teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ReadAllTeacherResponse {

    private Long teacherId;
    private String teacherImage;
    private String teacherName;
    private String teacherSubject;

    public static ReadAllTeacherResponse of(Teacher teacher, String imageUrl) {
        return ReadAllTeacherResponse.builder()
                .teacherSubject(teacher.getSubject())
                .teacherImage(imageUrl)
                .teacherName(teacher.getName())
                .teacherId(teacher.getId())
                .build();
    }
}