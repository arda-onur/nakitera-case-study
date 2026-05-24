package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends RuntimeException {
    private Object[] args;
    public CustomerNotFoundException(String message, Object... args) {
        super(message);
        this.args = args;
    }
}
