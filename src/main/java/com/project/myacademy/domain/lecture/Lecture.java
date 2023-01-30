package com.project.myacademy.domain.lecture;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.employee.Employee;
import com.project.myacademy.domain.lecture.dto.CreateLectureRequest;
import com.project.myacademy.domain.lecture.dto.UpdateLectureRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;

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

    @Column(name = "minimum_capacity")
    private Integer minimumCapacity;

    @Column(name = "maximum_capacity")
    private Integer maximumCapacity;

    @Column(name = "lecture_day")
    private String LectureDay;

    @Column(name = "lecture_time")
    private String LectureTime;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "first_register_employee")
    private String registerEmployee;

    @Column(name = "last_modified_employee")
    private String modifiedEmployee;

    @Column(name = "current_enrollment_number")
    private Integer currentEnrollmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // 강좌 생성 메서드
    public static Lecture addLecture(Employee employee, Employee teacher, CreateLectureRequest request) {
        StringBuilder sb = new StringBuilder();
        return Lecture.builder()
                .name(request.getLectureName())
                .price(request.getLecturePrice())
                .minimumCapacity(request.getMinimumCapacity())
                .maximumCapacity(request.getMaximumCapacity())
                .LectureDay(request.getLectureDay())
                .LectureTime(request.getLectureTime())
                .startDate(request.getStartDate())
                .finishDate(request.getFinishDate())
                .employee(teacher)
                .registerEmployee(sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString())
                .modifiedEmployee(sb.toString())
                .currentEnrollmentNumber(0)
                .build();
    }

    // 강좌 수정 메서드
    public void updateLecture(Employee employee, UpdateLectureRequest request) {
        StringBuilder sb = new StringBuilder();
        this.name = request.getLectureName();
        this.price = request.getLecturePrice();
        this.minimumCapacity = request.getMinimumCapacity();
        this.maximumCapacity = request.getMaximumCapacity();
        this.LectureDay = request.getLectureDay();
        this.LectureTime = request.getLectureTime();
        this.startDate = request.getStartDate();
        this.finishDate = request.getFinishDate();
        this.modifiedEmployee = sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString();
//        this.employee = teacher;
    }

    // 강좌 삭제 전에 마지막 수정 직원 필드 삭제 직원으로 업데이트
    public void recordDeleteEmployee(Employee employee) {
        StringBuilder sb = new StringBuilder();
        this.modifiedEmployee = sb.append(employee.getId()).append(" (").append(employee.getName()).append(")").toString();
    }

    // 현재 등록인원 +
    public void plusCurrentEnrollmentNumber() {
        this.currentEnrollmentNumber += 1;
    }

    // 현재 등록인원 -
    public void minusCurrentEnrollmentNumber() {
        this.currentEnrollmentNumber -= 1;
    }
}
