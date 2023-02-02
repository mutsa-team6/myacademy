package com.project.myacademy.domain.uniqueness.util;

import com.project.myacademy.domain.parent.Parent;
import com.project.myacademy.domain.student.Student;

public enum StudentFixtureUtil {
    STUDENT1(1L, "name", "school", "birth", "phoneNum", "email"),
    STUDENT2(2L, "name", "school", "birth", "phoneNum", "email");

    private Long id;
    private String name;
    private String school;
    private String birth;
    private String phoneNum;
    private String email;

    StudentFixtureUtil(Long id, String name, String school, String birth, String phoneNum, String email) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.birth = birth;
        this.phoneNum = phoneNum;
        this.email = email;
    }

    public Student init() {
        return Student.builder()
                .id(this.id)
                .name(this.name)
                .school(this.school)
                .birth(this.birth)
                .phoneNum(this.phoneNum)
                .email(this.email)
                .build();
    }
}
