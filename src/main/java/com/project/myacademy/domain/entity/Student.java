package com.project.myacademy.domain.entity;

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

    @Column(name = "phone_number")
    private String phoneNum;

    private String email;

    private String address;

    // 학생 : 부모 = 다 : 1 ( 자녀가 여러명이므로 )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;
}
