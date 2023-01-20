package com.project.myacademy.domain.payment;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.studentlecture.StudentLecture;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "payment_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE payment_tb SET deleted_at = current_timestamp WHERE payment_id = ?")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @OneToMany(mappedBy = "payment")
    private List<StudentLecture> studentLectures;

}
