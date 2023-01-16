package com.project.myacademy.domain.entity;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "lecture_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE lecture_tb SET deleted_at = current_timestamp WHERE lecture_id = ?")
public class Lecture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    private String name;

    private Integer price;

    @Column(name = "minimum_capcity")
    private Integer minimumCapacity;

    @Column(name = "maximum_capcity")
    private Integer maximumCapacity;

    @Column(name = "lecture_day")
    private String LectureDay;

    @Column(name = "lecture_time")
    private String LectureTime;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "finish_date")
    private LocalDateTime finishDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;


}
