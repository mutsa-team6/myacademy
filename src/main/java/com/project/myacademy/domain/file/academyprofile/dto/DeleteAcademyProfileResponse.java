package com.project.myacademy.domain.file.academyprofile.dto;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.file.employeeprofile.dto.DeleteEmployeeProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DeleteAcademyProfileResponse {

    private Long deletedAcademy;
    private String message;

    public static DeleteAcademyProfileResponse of(Academy academy) {
        return DeleteAcademyProfileResponse.builder()
                .deletedAcademy(academy.getId())
                .message("삭제 성공")
                .build();
    }
}
