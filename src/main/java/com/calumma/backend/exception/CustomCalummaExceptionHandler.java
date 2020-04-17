package com.calumma.backend.exception;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.calumma.backend.model.request.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class CustomCalummaExceptionHandler {

    @ExceptionHandler(NotAcceptableParametersException.class)
    public ResponseEntity<RequestError> customHandleNotFound(NotAcceptableParametersException ex, WebRequest request) throws JsonProcessingException {
        return new ResponseEntity<>(ex.getRequest(), HttpStatus.NOT_ACCEPTABLE);
    }

}
