package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {
    Object[] args;
    public OrderNotFoundException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
