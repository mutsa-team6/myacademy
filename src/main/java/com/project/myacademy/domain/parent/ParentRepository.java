package com.project.myacademy.domain.parent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByPhoneNumAndAcademyId(String phoneNum, Long academyId);

    Optional<Parent> findByIdAndAcademyId(Long parentId, Long academyId);

    Boolean existsByPhoneNum(String parentNum);
}
