package com.project.myacademy.domain.student.dto;

import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class DeleteStudentResponse {
    //학생 id
    private Long id;
    //학생 이름
    private String name;

    public static DeleteStudentResponse of(Student student) {
        return DeleteStudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .build();
    }
}
