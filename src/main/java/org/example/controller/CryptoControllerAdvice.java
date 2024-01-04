package org.example.controller;

import org.example.exception.ErrorMessage;
import org.example.exception.InvalidDateException;
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
    @ExceptionHandler(value = {
            InvalidDateException.class,
            DateTimeException.class,
            DateTimeParseException.class,
            UnsupportedTemporalTypeException.class,
    })
    public ResponseEntity<Object> handleConflict(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, new ErrorMessage("Date is not valid"),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
