package com.ardao.nakitera_case_study.controller;

import com.ardao.nakitera_case_study.util.mapper.user.UserMapper;
import com.ardao.nakitera_case_study.request.customer.UserRequest;
import com.ardao.nakitera_case_study.response.UserResponse;
import com.ardao.nakitera_case_study.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Authentication and user registration endpoints")
public class  AuthController {
    private final AuthService authService;
    private final MessageSource messageSource;

    public AuthController(AuthService authService, MessageSource messageSource) {
        this.authService = authService;
        this.messageSource = messageSource;
    }
    @Operation(summary = "Create user", description = "Creates a customer user account.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Username already exists"),
            @ApiResponse(responseCode = "429", description = "Too many requests")
    })
     @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest,  Locale locale){
         this.authService.createUser(UserMapper.toEntity(userRequest));
         return ResponseEntity.status(HttpStatus.CREATED).body(
                                            new UserResponse(userRequest.username(),
                                                    this.messageSource.getMessage("user.response.created",
                                                                                            null,  locale)));
    }
}
