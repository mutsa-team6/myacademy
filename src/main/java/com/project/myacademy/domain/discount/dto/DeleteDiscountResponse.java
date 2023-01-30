package com.project.myacademy.domain.discount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class DeleteDiscountResponse {

    private Long discountId;
    private String message;

    public static DeleteDiscountResponse of(Long discountId) {
        return DeleteDiscountResponse.builder()
                .discountId(discountId)
                .message("할인정책 삭제 성공")
                .build();
    }
}