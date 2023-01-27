package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.employee.EmployeeRole;
import com.project.myacademy.domain.lecture.Lecture;
import com.project.myacademy.domain.teacher.dto.CreateTeacherRequest;
import com.project.myacademy.domain.teacher.dto.UpdateTeacherRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "teacher_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE teacher_tb SET deleted_at = current_timestamp WHERE teacher_id = ?")
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long id;
    private String name;
    private String subject;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @OneToMany(mappedBy = "teacher")
    private List<Lecture> lectures;

    // 직원 중 강사 확인해서 강사 테이블에 삽입
    public static Teacher addTeacher(CreateTeacherRequest request, Employee employee) {
        return Teacher.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .employee(employee)
                .build();
    }

    // 직원이 강사 테이블에 등록될 때 직원의 권한이 강사인지를 확인하는 메서드
    // 강사 테이블에 등록되는 사람의 권한을 확인하는 것이지 강사 테이블을 등록하는 주체의 권한을 확인하는 메서드가 아님
    public static boolean isTeacher(Employee employee) {
        if(employee.getEmployeeRole().equals(EmployeeRole.ROLE_USER)) return true;
        else return false;
    }

    // 강사 수정
    public void updateTeacher(UpdateTeacherRequest request) {
        this.name = request.getName();
        this.subject = request.getSubject();
    }
}
