package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.BaseEntity;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

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

    private String name;

    private String address;

    @Column(name = "phone_number")
    private String phoneNum;

    private String email;

    private String account;

    @Column(name ="employee_role")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;
}
