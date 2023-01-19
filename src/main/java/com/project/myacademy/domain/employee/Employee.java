package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.dto.EmployeeDto;
import com.project.myacademy.domain.employee.dto.UpdateEmployeeRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "employee_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE employee_tb SET deleted_at = current_timestamp WHERE employee_id = ?")
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long id;

    @NotBlank
    private String name;

    private String address;

    @Column(name = "phone_number")
    @NotBlank
    private String phoneNum;

    @NotBlank
    private String email;

    @NotBlank
    private String account;

    @Column(name ="employee_role")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    @NotBlank
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;

    public EmployeeDto toEmployeeDto() {
        return EmployeeDto.builder()
                .id(this.id)
                .name(this.name)
                .account(this.account)
                .build();
    }

    public void update(UpdateEmployeeRequest request) {
        this.name = request.getName();
        this.address = request.getAddress();
        this.phoneNum = request.getPhoneNum();
        this.email = request.getEmail();
        this.password = request.getPassword();
    }
}
