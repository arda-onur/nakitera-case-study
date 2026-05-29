package com.ardao.nakitera_case_study.exception;


import com.ardao.nakitera_case_study.exception.custom.*;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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


    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFoundException(OrderNotFoundException ex, Locale locale){
        String message = this.messageSource.getMessage(ex.getMessage(),ex.getArgs(),ex.getMessage(),locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex, Locale locale) {
        String message = this.messageSource.getMessage(
                ex.getMessage(),
                null,
                ex.getMessage(),
                locale
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    @ExceptionHandler(OrderCannotBeCanceledException.class)
    public ResponseEntity<String> handleOrderCannotBeCanceledException(OrderCannotBeCanceledException ex, Locale locale) {
        String message = this.messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler({HandlerMethodValidationException.class,
                      MethodArgumentTypeMismatchException.class,
                      ConstraintViolationException.class,})
    public ResponseEntity<String> handleHandlerMethodValidationException(Exception ex,
                                                                           Locale locale) {
        String key = "request.parameters.invalid.exception";
        String message = this.messageSource.getMessage(key, null, key, locale);
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(InvalidOrderSideException.class)
    public ResponseEntity<String> handleInvalidOrderSideException(InvalidOrderSideException ex, Locale locale) {
        String message = this.messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return ResponseEntity.badRequest().body(message);
    }
    @ExceptionHandler(InvalidOrderAssetException.class)
    public ResponseEntity<String> handleInvalidOrderAssetException(InvalidOrderAssetException ex, Locale locale) {
        String message = this.messageSource.getMessage(ex.getMessage(), ex.getArgs(), ex.getMessage(), locale);
        return ResponseEntity.badRequest().body(message);
    }
}
