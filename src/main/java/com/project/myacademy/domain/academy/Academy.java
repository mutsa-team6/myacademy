package com.project.myacademy.domain.academy;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.dto.AcademyDto;
import com.project.myacademy.domain.academy.dto.CreateAcademyRequest;
import com.project.myacademy.domain.academy.dto.UpdateAcademyReqeust;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "academy_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE academy_tb SET deleted_at = current_timestamp WHERE academy_id = ?")
public class Academy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "academy_id")
    private Long id;

    private String name;

    private String address;

    @Column(name = "phone_number")
    private String phoneNum;

    private String owner;

    private String businessRegistrationNumber;

    public Academy(String name, String address, String phoneNum, String owner, String businessRegistrationNumber) {
        this.name = name;
        this.address = address;
        this.phoneNum = phoneNum;
        this.owner = owner;
        this.businessRegistrationNumber = businessRegistrationNumber;
    }

    public static Academy createAcademy(CreateAcademyRequest request) {
        return new Academy(request.getName(), request.getAddress(), request.getPhoneNum(), request.getOwner(), request.getBusinessRegistrationNumber());
    }

//    public AcademyDto toAcademyDto() {
//        return new AcademyDto(this.id, this.name, this.owner, "");
//    }

//    public void update(UpdateAcademyReqeust reqeust) {
//        this.name = reqeust.getName();
//        this.address = reqeust.getAddress();
//        this.phoneNum = reqeust.getPhoneNum();
//        this.owner = reqeust.getOwner();
//        this.businessRegistrationNumber = reqeust.getBusinessRegistrationNumber();
//    }
}
