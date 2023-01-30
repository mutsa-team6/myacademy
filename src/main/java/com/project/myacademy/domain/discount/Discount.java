package com.project.myacademy.domain.discount;

import com.project.myacademy.domain.BaseEntity;
import com.project.myacademy.domain.academy.Academy;
import com.project.myacademy.domain.discount.dto.CreateDiscountRequest;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "discount_tb")
@Where(clause = "deleted_at is NULL")
@SQLDelete(sql = "UPDATE discount_tb SET deleted_at = current_timestamp WHERE discount_id = ?")
public class Discount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Long id;

    private String discountName;
    private Integer discountRate;
    private Long academyId;

    public static Discount makeDiscount(CreateDiscountRequest request, Academy academy) {
        return Discount.builder()
                .discountName(request.getDiscountName())
                .discountRate(request.getDiscountRate())
                .academyId(academy.getId())
                .build();
    }
}