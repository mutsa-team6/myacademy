package com.project.myacademy.domain.announcement;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.announcement.dto.CreateAnnouncementRequest;
import com.project.myacademy.domain.announcement.dto.UpdateAnnouncementRequest;
import com.project.myacademy.domain.employee.Employee;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "announcement_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE announcement_tb SET deleted_at = current_timestamp WHERE announcement_id = ?")
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long id;

    private String title;

    @Lob
    private String body;

    @Enumerated(value = EnumType.STRING)
    private AnnouncementType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;


    public void updateAnnouncement(UpdateAnnouncementRequest request) {
        this.title = request.getTitle();
        this.body = request.getBody();
    }

    public static Announcement toAnnouncement(CreateAnnouncementRequest request, Academy academy,Employee employee) {
        return Announcement.builder()
                .title(request.getTitle())
                .type(request.getType())
                .body(request.getBody())
                .employee(employee)
                .academy(academy)
                .build();
    }
}
