package com.project.myacademy.domain.file.teacherprofile;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.teacher.Teacher;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "teacher_profile_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE teacher_profile_tb SET deleted_at = current_timestamp WHERE teacher_profile_id = ?")
public class TeacherProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_profile_id")
    private Long id;

    private String uploadFileName;
    private String storedFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    public static TeacherProfile makeTeacherProfile(String uploadFileName, String storedFileUrl, Teacher teacher) {
        return TeacherProfile.builder()
                .uploadFileName(uploadFileName)
                .storedFileUrl(storedFileUrl)
                .teacher(teacher)
                .build();
    }
}