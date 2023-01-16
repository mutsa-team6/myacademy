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
@Table(name = "teacher_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE teacher_tb SET deleted_at = current_timestamp WHERE teacher_id = ?")
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Long id;

    private String name;


    @Column(name = "phone_number")
    private String phoneNum;

    private String email;

    private String address;

}
