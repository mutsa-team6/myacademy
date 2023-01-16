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
@Table(name = "uniqueness_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE uniqueness_tb SET deleted_at = current_timestamp WHERE uniqueness_id = ?")
public class Uniqueness extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uniqueness_id")
    private Long id;

    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;
}
