package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;

@Getter
public class  UserAlreadyExistException extends RuntimeException {

    private final Object[] args;

    public UserAlreadyExistException(String message, Object... args)  {
        super(message);
        this.args = args;

    }
}
