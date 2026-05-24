package com.ardao.nakitera_case_study.exception;


import com.ardao.nakitera_case_study.exception.custom.CustomerIdRequiredException;
import com.ardao.nakitera_case_study.exception.custom.CustomerNotFoundException;
import com.ardao.nakitera_case_study.exception.custom.UserAlreadyExistException;
import com.ardao.nakitera_case_study.exception.custom.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

     @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String>handleUsernameNotFoundException(UserNotFoundException ex, Locale locale){
       String message = this.messageSource.getMessage(ex.getMessage(),ex.getArgs(),ex.getMessage(),locale);
       return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<String> handleUserAlreadyExistException(UserAlreadyExistException ex, Locale locale){
        String message = this.messageSource.getMessage(ex.getMessage(),ex.getArgs(),ex.getMessage(),locale);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "\n" + msg2)
                .get();
        return ResponseEntity.badRequest().body(message);
    }
    @ExceptionHandler(CustomerIdRequiredException.class)
    public ResponseEntity<String> handleCustomerIdRequiredException(CustomerIdRequiredException ex, Locale locale) {
        String message = this.messageSource.getMessage(ex.getMessage(), null, ex.getMessage(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> handleCustomerNotFoundException(CustomerNotFoundException ex, Locale locale){
        String message = this.messageSource.getMessage(ex.getMessage(),ex.getArgs(),ex.getMessage(),locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

}
