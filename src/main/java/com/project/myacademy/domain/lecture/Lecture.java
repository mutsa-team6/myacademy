package com.project.myacademy.domain.lecture;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.lecture.dto.CreateLectureRequest;
import com.project.myacademy.domain.lecture.dto.UpdateLectureRequest;
import com.project.myacademy.domain.teacher.Teacher;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate startDate;

    @Column(name = "finish_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    private LocalDate finishDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    public static Lecture addLecture(CreateLectureRequest request, Teacher teacher) {
        return Lecture.builder()
                .name(request.getLectureName())
                .price(request.getLecturePrice())
                .minimumCapacity(request.getMinimumCapacity())
                .maximumCapacity(request.getMaximumCapacity())
                .LectureDay(request.getLectureDay())
                .LectureTime(request.getLectureTime())
                .startDate(request.getStartDate())
                .finishDate(request.getFinishDate())
                .teacher(teacher)
                .build();
    }

    public void updateLecture(UpdateLectureRequest request) {
        this.name = request.getLectureName();
        this.price = request.getLecturePrice();
        this.minimumCapacity = request.getMinimumCapacity();
        this.maximumCapacity = request.getMaximumCapacity();
        this.LectureDay = request.getLectureDay();
        this.LectureTime = request.getLectureTime();
        this.startDate = request.getStartDate();
        this.finishDate = request.getFinishDate();
    }

}
