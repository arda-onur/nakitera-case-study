package com.ardao.nakitera_case_study.exception.custom;

import lombok.Getter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
@Getter
public class UserNotFoundException extends UsernameNotFoundException {
    private final Object[] args;

    public UserNotFoundException(String message,Object... args) {
        super(message);
        this.args = args;
    }
}
