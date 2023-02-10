package com.project.myacademy.domain.file.academyprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyProfileRepository extends JpaRepository<AcademyProfile, Long> {

    Optional<AcademyProfile> findByAcademy_Id(Long id);

    Boolean existsAcademyProfileByAcademy_Id(Long academyId);
}
