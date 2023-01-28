package com.project.myacademy.domain.file.employeeprofile;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "employee_profile_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE employee_profile_tb SET deleted_at = current_timestamp WHERE employee_profile_id = ?")
public class EmployeeProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_profile_id")
    private Long id;

    private String uploadFileName;
    private String storedFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private Long uploadEmployee;

    public static EmployeeProfile makeEmployeeProfile(String uploadFileName, String storedFileUrl, Employee employee, Employee uploadEmployee) {
        return EmployeeProfile.builder()
                .uploadFileName(uploadFileName)
                .storedFileUrl(storedFileUrl)
                .employee(employee)
                .uploadEmployee(uploadEmployee.getId())
                .build();
    }
}