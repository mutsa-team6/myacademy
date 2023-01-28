package com.project.myacademy.domain.file.announcementfile.dto;

import com.project.myacademy.domain.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DeleteAnnoucementFileResponse {

    private Long deletedEmployee;
    private String message;

    public static DeleteAnnoucementFileResponse of(Employee employee) {
        return DeleteAnnoucementFileResponse.builder()
                .deletedEmployee(employee.getId())
                .message("삭제 성공")
                .build();
    }
}
