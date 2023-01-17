package com.project.myacademy.domain.student.dto;

import com.project.myacademy.domain.student.Student;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindStudentResponse {
    //학생 Id
    private Long id;
    //학생 이름
    private String name;
    //학생 주소
    private String address;
    //학생 생년월일
    private String birth;
    //학생 핸드폰번호
    private String phoneNum;
    //학생 이메일
    private String email;
    //부모 핸드폰번호
    private String parentPhoneNum;

    public static FindStudentResponse of(Student student) {
        return FindStudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .address(student.getAddress())
                .birth(student.getBirth())
                .phoneNum(student.getPhoneNum())
                .email(student.getEmail())
                //.parentPhoneNum(student.getParent().getPhoneNum()) NPE 제거해야함
                .build();
    }
}
