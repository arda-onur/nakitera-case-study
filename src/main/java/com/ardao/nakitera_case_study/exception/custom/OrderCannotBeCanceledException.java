package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class OrderCannotBeCanceledException extends RuntimeException {
    private final Object[] args;

    public OrderCannotBeCanceledException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
