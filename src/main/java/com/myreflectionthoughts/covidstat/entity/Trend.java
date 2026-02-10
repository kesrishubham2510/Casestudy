package com.myreflectionthoughts.covidstat.entity;

import lombok.Data;

@Data
public class Trend extends ResponseWrapper{
    private String dailyAverage;
    private double changePercentage;
    private String direction;
    private String alertMessage;
}
