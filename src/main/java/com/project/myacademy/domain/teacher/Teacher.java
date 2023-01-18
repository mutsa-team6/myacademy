package com.project.myacademy.domain.teacher;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
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

    public static Teacher addTeacherToLecture(CreateTeacherRequest request, Employee employee) {
        return Teacher.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .employee(employee)
                .build();
    }

    public void updateTeacherInLecture(UpdateTeacherRequest request) {
        this.name = request.getName();
        this.subject = request.getSubject();
    }
}
