package com.project.myacademy.domain.employee;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.employee.dto.CreateEmployeeRequest;
import com.project.myacademy.domain.employee.dto.UpdateEmployeeRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import static com.project.myacademy.domain.employee.EmployeeRole.ROLE_USER;

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
    private String phoneNum;

    @NotBlank
    private String email;

    @NotBlank
    private String account;

    @Column(name = "employee_role")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;

    private String subject;

    public void updateEmployeeInfo(UpdateEmployeeRequest request) {
        this.address = request.getAddress();
        this.phoneNum = request.getPhoneNum();
        this.subject = request.getSubject();
    }

    public void updatePasswordOnly(String password) {
        this.password = password;
    }

    // ADMIN, STAFF 가 사용하는 등급 변경 메서드
    public void changeRole(EmployeeRole employeeRole) {
        this.employeeRole = employeeRole;
    }

    // 등록, 수정, 삭제 작업을 진행하는 직원의 권한을 확인하는 메서드
    public static boolean isTeacherAuthority(Employee employee) {
        return employee.getEmployeeRole().equals(ROLE_USER);
    }

    // ADMIN 권한 직원 등록
    public static Employee createAdminEmployee(CreateEmployeeRequest request, Academy foundAcademy, String encryptedPassword) {
        return Employee.builder()
                .name(request.getName())
                .employeeRole(EmployeeRole.ROLE_ADMIN)
                .account("admin")
                .phoneNum(request.getPhoneNum())
                .email(request.getEmail())
                .address(request.getAddress())
                .academy(foundAcademy)
                .password(encryptedPassword)
                .subject(request.getSubject())
                .build();
    }

    // USER 권한 직원 등록
    // 직원인 경우 과목칸에 뭘 적거나 적지 않아도 그냥 "직원"으로 데이터가 입력
    public static Employee createStaffEmployee(CreateEmployeeRequest request, Academy foundAcademy, String encryptedPassword) {
        return Employee.builder()
                .name(request.getName())
                .employeeRole(EmployeeRole.ROLE_STAFF)
                .account(request.getAccount())
                .subject("직원")
                .phoneNum(request.getPhoneNum())
                .email(request.getEmail())
                .address(request.getAddress())
                .academy(foundAcademy)
                .password(encryptedPassword)
                .build();

    }

    // USER 권한 직원 등록
    public static Employee createUserEmployee(CreateEmployeeRequest request, Academy foundAcademy, String encryptedPassword) {
        return Employee.builder()
                .name(request.getName())
                .employeeRole(EmployeeRole.ROLE_USER)
                .account(request.getAccount())
                .subject(request.getSubject())
                .phoneNum(request.getPhoneNum())
                .email(request.getEmail())
                .address(request.getAddress())
                .academy(foundAcademy)
                .password(encryptedPassword)
                .build();
    }
}
