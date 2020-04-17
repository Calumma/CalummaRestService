package com.calumma.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends Exception {

    public ForbiddenException() {
        super("Sry. You do not have permission to do that :/");
    }
}
