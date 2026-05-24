package com.ardao.nakitera_case_study.request.customer;

import com.ardao.nakitera_case_study.annotation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

@PasswordsMatch
public record UserRequest(
        @NotBlank(message = "{user.request.validation.username.cannot.be.blank}")
        @Size(min = 3, message = "{user.request.validation.username.size}")
        String username,
        @NotBlank(message = "{user.request.validation.password.cannot.be.blank}")
        @Size(min = 5, message = "{user.request.validation.password.size}")
        String password,
        @NotBlank(message = "{user.request.validation.password.cannot.be.blank}")
        @Size(min = 5, message = "{user.request.validation.password.size}")
        String confirmPassword) {


    public UserRequest {
        if(username != null)  username = username.toLowerCase(Locale.ROOT).trim();
    }
}
