package com.project.myacademy.domain.file.academyprofile;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "academy_profile_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE academy_profile_tb SET deleted_at = current_timestamp WHERE academy_profile_id = ?")
public class AcademyProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_profile_id")
    private Long id;

    private String uploadFileName;
    private String storedFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academy_id")
    private Academy academy;

    public static AcademyProfile makeAcademyProfile(String uploadFileName, String storedFileUrl, Academy academy) {
        return AcademyProfile.builder()
                .uploadFileName(uploadFileName)
                .storedFileUrl(storedFileUrl)
                .academy(academy)
                .build();
    }
}
