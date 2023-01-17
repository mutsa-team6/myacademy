package com.project.myacademy.domain.student.dto;

import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
public class ModifyStudentResponse {
    //학생 id
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

    //학생 정보 마지막 수정 일시
    private LocalDateTime lastModifiedAt;

    public static ModifyStudentResponse of(Student student) {
        return ModifyStudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .address(student.getAddress())
                .birth(student.getBirth())
                .phoneNum(student.getPhoneNum())
                .email(student.getEmail())
                .build();

    }
}
