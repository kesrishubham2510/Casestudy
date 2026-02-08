package com.myreflectionthoughts.covidstat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalAPIResponse extends ResponseWrapper {
    private long updated;
    private String country;
    private CountryInfo countryInfo;
    private long cases;
    private long todayCases;
    private long deaths;
    private long todayDeaths;
    private long recovered;
    private long todayRecovered;
    private long active;
    private long critical;
    private double casesPerOneMillion;
    private double deathsPerOneMillion;
    private long tests;
    private double testsPerOneMillion;
    private long population;
    private String continent; // Note: Changed to String as continents are usually named
    private double oneCasePerPeople;
    private double oneDeathPerPeople;
    private double oneTestPerPeople;
    private double activePerOneMillion;
    private double recoveredPerOneMillion;
    private double criticalPerOneMillion;

    static class CountryInfo extends ResponseWrapper {
        private int _id;
        private String iso2;
        private String iso3;
        private double lat;
        @JsonProperty("long")
        private double longitude; // 'long' is a reserved keyword in Java
        private String flag;
    }
}