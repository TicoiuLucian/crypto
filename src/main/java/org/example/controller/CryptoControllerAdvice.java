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
          UnsupportedTemporalTypeException.class
  })
  public ResponseEntity<Object> handleDateExceptions(Exception ex, WebRequest request) {
    String errorMessage = "Date is not valid";
    HttpStatus status = HttpStatus.BAD_REQUEST;

    if (ex instanceof InvalidDateException) {
      errorMessage = "Invalid date format";
    } else if (ex instanceof DateTimeParseException) {
      errorMessage = "Invalid date format. Please provide the date in a valid format.";
    } else if (ex instanceof DateTimeException) {
      errorMessage = "Unsupported or invalid date/time operation.";
    }

    logger.error("Error occurred while processing date: " + ex.getMessage(), ex);

    return handleExceptionInternal(ex, new ErrorMessage(errorMessage), new HttpHeaders(), status, request);
  }

}
