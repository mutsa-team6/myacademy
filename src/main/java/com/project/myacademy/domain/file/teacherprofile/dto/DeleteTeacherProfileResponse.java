package com.project.myacademy.domain.file.teacherprofile.dto;

import com.project.myacademy.domain.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DeleteTeacherProfileResponse {

    private Long deletedEmployee;
    private String message;

    public static DeleteTeacherProfileResponse of(Employee employee) {
        return DeleteTeacherProfileResponse.builder()
                .deletedEmployee(employee.getId())
                .message("삭제 성공")
                .build();
    }
}