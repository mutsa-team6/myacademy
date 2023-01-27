package com.project.myacademy.domain.student;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.student.dto.CreateStudentRequest;
import com.project.myacademy.domain.student.dto.UpdateStudentRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "student_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE student_tb SET deleted_at = current_timestamp WHERE student_id = ?")
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    private String name;

    private String school;

    private String birth;

    @Column(name = "phone_number")
    private String phoneNum;

    private String email;

    private String address;

    // 학생 : 부모 = 다 : 1 ( 자녀가 여러명이므로 )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    //학원 Id
    private Long academyId;

    public void updateStudent(UpdateStudentRequest request) {
        this.name = request.getName();
        this.school = request.getSchool();
        this.birth = request.getSchool();
        this.phoneNum = request.getPhoneNum();
        this.email = request.getEmail();
        this.address = request.getAddress();
    }

    public static Student toStudent(CreateStudentRequest request, Parent parent, Long academyId) {
        return Student.builder()
                .name(request.getName())
                .school(request.getSchool())
                .birth(request.getBirth())
                .phoneNum(request.getPhoneNum())
                .email(request.getEmail())
                .address(request.getAddress())
                .parent(parent)
                .academyId(academyId)
                .build();
    }
}
