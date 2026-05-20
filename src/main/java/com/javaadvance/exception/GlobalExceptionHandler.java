package com.javaadvance.exception;


import com.javaadvance.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex){
        ErrorResponse body = new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Resource not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DublicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDublicateEmail(DublicateEmailException ex){
        ErrorResponse body = new ErrorResponse(HttpStatus.CONFLICT.value(), "Dublicate email", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(TooManyCardsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyCards(TooManyCardsException ex){
        ErrorResponse body = new ErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT.value(), "Too many cards", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException ex){
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Not valid variables", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


}
