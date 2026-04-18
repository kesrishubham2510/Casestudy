package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.contract.IResponsePopulator;
import com.myreflectionthoughts.covidstat.contract.IValidator;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;
import com.myreflectionthoughts.covidstat.exception.CountryNotFoundException;
import com.myreflectionthoughts.covidstat.exception.FallbackException;
import com.myreflectionthoughts.covidstat.utility.DataUtility;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.myreflectionthoughts.covidstat.constant.ServiceConstant._ERR_BAD_REQUEST_KEY;


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
    ) {

        this.currentStatsPopulator = currentStatsPopulator;
        this.countryValidator = countryValidator;
        this.vaccinationTrendsPopulator = vaccinationTrendsPopulator;
        this.alertMessagePopulator = alertMessagePopulator;
        this.logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    }

    public List<CovidStatResponse> fetchComparisionStats(Country[] countries, LocalDate referenceDate) {
        List<CovidStatResponse> statResponses = new ArrayList<>();

        // to filter out unique vlaid countries
        Set<Country> countrySet = Arrays.asList(countries).stream().filter(country -> countryValidator.isValid(country))
                .collect(Collectors.toSet());

        if (countrySet.size() < 2) {
            throw new CaseStudyException(_ERR_BAD_REQUEST_KEY, HttpStatus.BAD_REQUEST.value(), "Need atleast two countries to compare");
        }

        statResponses = countrySet.stream().map(country -> {
            return fetchStats(country, referenceDate);
        }).collect(Collectors.toList());

        return statResponses;
    }

    // TODO: Make it handle error and graceful degradation
    public CovidStatResponse fetchStats(Country country, LocalDate referencedDate) {

        String countryName = country.getDisplayName();

        try {
            CovidStatResponse covidStatResponse = currentStatsPopulator.populate(countryName, referencedDate.toString());
            covidStatResponse.setTrends(vaccinationTrendsPopulator.populate(countryName, referencedDate.toString()).getTrends());
            covidStatResponse.setAlertMessage(alertMessagePopulator.populate(countryName, referencedDate.toString()));

            return covidStatResponse;
        } catch (FallbackException fallbackException) {
            logger.severe("Returning Static response, because of Fallback exception");
            return DataUtility.getDefaultResponse("data/StaticCovidStatResponse.json", CovidStatResponse.class);
        }
    }

}
