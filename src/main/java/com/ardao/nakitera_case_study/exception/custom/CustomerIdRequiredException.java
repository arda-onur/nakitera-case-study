package com.ardao.nakitera_case_study.exception.custom;

public class CustomerIdRequiredException extends RuntimeException {
    public CustomerIdRequiredException(String message) {
        super(message);
    }
}
