package com.ardao.nakitera_case_study.annotation.validator;

import com.ardao.nakitera_case_study.annotation.PasswordsMatch;
import com.ardao.nakitera_case_study.request.customer.UserRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch,UserRequest>{

    @Override
    public boolean isValid(UserRequest userRequest, ConstraintValidatorContext context) {

        if (userRequest == null) {
            return true;
        }

        if (userRequest.password() == null || userRequest.confirmPassword() == null) {
            return true;
        }

        boolean valid = userRequest.password().equals(userRequest.confirmPassword());

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return valid;
    }
}
