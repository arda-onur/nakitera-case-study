package com.ardao.nakitera_case_study.annotation;

import com.ardao.nakitera_case_study.annotation.validator.PasswordsMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
@Documented
public @interface PasswordsMatch {
    String message() default "{user.request.validation.passwords.not.match}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
