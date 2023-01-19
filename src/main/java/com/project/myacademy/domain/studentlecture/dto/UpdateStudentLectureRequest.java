package com.project.myacademy.domain.studentlecture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class UpdateStudentLectureRequest {

    private String memo;
}
