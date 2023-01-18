package com.project.myacademy.domain.teacher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @Query(value = "SELECT t from Teacher t WHERE t.name = :name",
            countQuery = "SELECT COUNT(t) FROM Teacher t WHERE t.name = :name")
    Page<Teacher> findByName(String name, Pageable pageable);

}
