package com.javaadvance.exception;

public class DublicateEmailException extends RuntimeException {
    public DublicateEmailException(String message) {
        super(message);
    }
}
