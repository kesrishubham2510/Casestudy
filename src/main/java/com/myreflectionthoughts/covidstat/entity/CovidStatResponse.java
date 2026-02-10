package com.myreflectionthoughts.covidstat.entity;

import lombok.Data;

import java.util.Map;

@Data
public class CovidStatResponse extends ResponseWrapper{
    private String country;
    private String noOfCases;
    private String noOfRecoveries;
    private String activeAsToday;
    private Map<String, Trend> trends;
    private String alertMessage;
}
