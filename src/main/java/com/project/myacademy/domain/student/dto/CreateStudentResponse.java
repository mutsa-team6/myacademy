package com.project.myacademy.domain.student.dto;

import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@Builder
public class CreateStudentResponse {
    //학생 id
    private Long id;
    //학생 이름
    private String name;

    public static CreateStudentResponse of(Student student) {
        return CreateStudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .build();
    }
}
