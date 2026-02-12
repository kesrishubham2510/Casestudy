package com.myreflectionthoughts.covidstat.entity;

import lombok.Data;

import java.util.Map;

@Data
public class CovidStatResponse extends ResponseWrapper{
    private String country;
    private String noOfCases;
    private String noOfRecoveries;
    private String activeAsToday;
    private String dosesAdministeredInCountry;
    private String dosesAdministeredGlobally;
    private Map<String, Trends> trends;
    private String alertMessage;
}
