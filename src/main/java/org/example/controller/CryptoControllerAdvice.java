package org.example.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.time.temporal.UnsupportedTemporalTypeException;

@ControllerAdvice
public class CryptoControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = {DateTimeException.class, DateTimeParseException.class, UnsupportedTemporalTypeException.class, })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Month is not valid";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}
