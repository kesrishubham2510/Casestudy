package com.myreflectionthoughts.covidstat.exception;

import lombok.Data;

@Data
public class CaseStudyException extends RuntimeException{
    private final String key;
    private final int statusCode;
    private String message;

    public CaseStudyException(String key, int statusCode, String message){
        super(message);
        this.key = key;
        this.statusCode = statusCode;
    }
    public CaseStudyException(String key, String message){
        this(key, -1, message);
    }
}
