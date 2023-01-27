package com.project.myacademy.domain.file.employeeprofile.dto;

import com.project.myacademy.domain.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DeleteEmployeeProfileResponse {

    private Long deletedEmployee;
    private String message;

    public static DeleteEmployeeProfileResponse of(Employee employee) {
        return DeleteEmployeeProfileResponse.builder()
                .deletedEmployee(employee.getId())
                .message("삭제 성공")
                .build();
    }
}