package com.project.myacademy.domain.parent;

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
@Table(name = "parent_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE parent_tb SET deleted_at = current_timestamp WHERE parent_id = ?")
public class Parent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private Long id;

    private String name;

    @Column(name = "parent_recognized_code")
    private Integer parentRecognizedCode;

    @Column(name = "phone_number",unique = true)
    private String phoneNum;

    private String address;


}
