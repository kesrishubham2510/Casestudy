package com.myreflectionthoughts.covidstat.entity;

import java.util.Map;

public class CovidStatResponse extends ResponseWrapper{
    private String noOfCases;
    private String noOfRecoveries;
    private String activeAsToday;
    private Map<String, Trend> trends;
    private String alertMessage;
}
