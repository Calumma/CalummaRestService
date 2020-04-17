package com.calumma.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends Exception {

    public BadRequestException() {
        super("Sry. You must login first to try that :/");
    }

    public BadRequestException(String message) {
        super(message);
    }

}
