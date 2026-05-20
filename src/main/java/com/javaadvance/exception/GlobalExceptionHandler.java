package com.javaadvance.exception;


import com.javaadvance.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex){
        ErrorResponse body = new ErrorResponse(404, "Resource not found", ex.getMessage());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(DublicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDublicateEmail(DublicateEmailException ex){
        ErrorResponse body = new ErrorResponse(409, "Dublicate email", ex.getMessage());
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(TooManyCardsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyCards(TooManyCardsException ex){
        ErrorResponse body = new ErrorResponse(422, "Too many cards", ex.getMessage());
        return ResponseEntity.status(422).body(body);
    }


}
