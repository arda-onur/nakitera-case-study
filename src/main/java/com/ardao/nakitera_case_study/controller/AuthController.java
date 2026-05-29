package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.util.mapper.user.UserMapper;
import com.ardao.nakitera_case_study.request.customer.UserRequest;
import com.ardao.nakitera_case_study.response.UserResponse;
import com.ardao.nakitera_case_study.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/auth")
public class  AuthController {
    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }
     @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest,  Locale locale){
         this.authService.createUser(UserMapper.toEntity(userRequest));
         return ResponseEntity.status(HttpStatus.CREATED).body(
                                            new UserResponse(userRequest.username(),
                                                    this.messageSource.getMessage("user.response.created",
                                                                                            null,  locale)));
    }
}
