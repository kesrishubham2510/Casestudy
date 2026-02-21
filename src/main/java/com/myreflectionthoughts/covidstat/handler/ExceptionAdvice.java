package com.myreflectionthoughts.covidstat.handler;

import com.myreflectionthoughts.covidstat.exception.CountryNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<String> handleCountryNotFound(CountryNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }
}
