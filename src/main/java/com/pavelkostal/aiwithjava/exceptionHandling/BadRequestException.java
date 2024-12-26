package com.pavelkostal.aiwithjava.exceptionHandling;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
