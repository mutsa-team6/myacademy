package com.project.myacademy.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayType {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌");

    private String name;
}
