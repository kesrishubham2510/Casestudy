package com.myreflectionthoughts.covidstat.exception;

public class CountryNotFoundException extends RuntimeException{

    public CountryNotFoundException(String message) {
        super(message);
    }
}
