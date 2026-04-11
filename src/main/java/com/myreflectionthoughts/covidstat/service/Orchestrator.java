package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.contract.*;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.exception.CountryNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@Service
public class Orchestrator {

    private final IResponsePopulator<String, CovidStatResponse> currentStatsPopulator;
    private final IResponsePopulator<String, CovidStatResponse> vaccinationTrendsPopulator;
    private final IResponsePopulator<String, String> alertMessagePopulator;
    private final IValidator<Country> countryValidator;

    private final Logger logger;

    public Orchestrator(
                        @Qualifier(value = "currentStatPopulator")
                        IResponsePopulator<String, CovidStatResponse> currentStatsPopulator,
                        @Qualifier(value = "vaccinationTrendsPopulator")
                        IResponsePopulator<String, CovidStatResponse> vaccinationTrendsPopulator,
                        @Qualifier(value = "alertMessagePopulator")
                        IResponsePopulator<String, String> alertMessagePopulator,
                        IValidator<Country> countryValidator
                        ){

        this.currentStatsPopulator = currentStatsPopulator;
        this.countryValidator = countryValidator;
        this.vaccinationTrendsPopulator = vaccinationTrendsPopulator;
        this.alertMessagePopulator = alertMessagePopulator;
        this.logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    }

    public List<CovidStatResponse> fetchComparisionStats(Country[] countries, LocalDate referenceDate){
        List<CovidStatResponse> statResponses = new ArrayList<>();

        List<Country> countryList = Arrays.asList(countries).stream().filter(country -> countryValidator.isValid(country))
                .collect(Collectors.toList());

        if(countryList.size()<2){
            throw new CountryNotFoundException("Need atleast two countries to compare");
        }

        statResponses = countryList.stream().map(country -> {
            return fetchStats(country, referenceDate);
        }).collect(Collectors.toList());

        return statResponses;
    }

    // TODO: Make it handle error and graceful degradation
    public CovidStatResponse fetchStats(Country country, LocalDate referencedDate){

        String countryName = country.getDisplayName();
        CovidStatResponse covidStatResponse = currentStatsPopulator.populate(countryName, referencedDate.toString());
        covidStatResponse.setTrends(vaccinationTrendsPopulator.populate(countryName, referencedDate.toString()).getTrends());
        covidStatResponse.setAlertMessage(alertMessagePopulator.populate(countryName, referencedDate.toString()));

        return covidStatResponse;
    }

}
