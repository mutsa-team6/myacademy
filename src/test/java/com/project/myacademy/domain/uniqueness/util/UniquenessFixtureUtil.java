package com.project.myacademy.domain.uniqueness.util;

import com.project.myacademy.domain.student.Student;
import com.project.myacademy.domain.uniqueness.Uniqueness;

public enum UniquenessFixtureUtil {
    UNIQUENESS1(1L, "body", StudentFixtureUtil.STUDENT1.init()),
    UNIQUENESS2(2L, "body", StudentFixtureUtil.STUDENT1.init());

    private Long id;
    private String body;
    private Student student;

    UniquenessFixtureUtil(Long id, String body, Student student) {
        this.id = id;
        this.body = body;
        this.student = student;
    }

    public Uniqueness init() {
        return Uniqueness.builder()
                .id(this.id)
                .body(this.body)
                .student(this.student)
                .build();
    }
}
