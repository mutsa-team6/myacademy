package com.project.myacademy.domain.student.dto;

import com.project.myacademy.domain.student.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReadAllStudentResponse {
    //학원 Id
    private Long academyId;
    //학생 Id
    private Long id;
    //학생 이름
    private String name;
    //학생 생년월일
    private String birth;
    //학생 핸드폰번호
    private String phoneNum;
    //학생 이메일
    private String email;
    //학생 학교
    private String school;
    //부모 핸드폰번호
    private String parentPhoneNum;
    //부모 주소
    private String address;
    //학생 정보 등록 일시
    private LocalDateTime createAt;
    //학생 정보 마지막 수정 일시
    private LocalDateTime lastModifiedAt;

    public static ReadAllStudentResponse of(Student student) {
        return ReadAllStudentResponse.builder()
                .academyId(student.getAcademyId())
                .id(student.getId())
                .name(student.getName())
                .birth(student.getBirth())
                .phoneNum(student.getPhoneNum())
                .email(student.getEmail())
                .school(student.getSchool())
                .parentPhoneNum(student.getParent().getPhoneNum())
                .address(student.getParent().getAddress())
                .createAt(student.getCreatedAt())
                .lastModifiedAt(student.getLastModifiedAt())
                .build();
    }
}
