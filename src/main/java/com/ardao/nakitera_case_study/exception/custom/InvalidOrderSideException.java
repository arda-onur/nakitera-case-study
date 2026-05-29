package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class InvalidOrderSideException extends RuntimeException {
    private final Object[] args;

    public InvalidOrderSideException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}