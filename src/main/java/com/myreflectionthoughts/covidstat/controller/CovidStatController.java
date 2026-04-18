package com.myreflectionthoughts.covidstat.controller;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.service.Orchestrator;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.WeekFields.ISO;

@RestController
@RequestMapping(ServiceConstant.API_PREFIX)
public class CovidStatController {

    private final Orchestrator orchestrator;

    public CovidStatController(Orchestrator orchestrator){
        this.orchestrator = orchestrator;
    }


    @ApiResponse(responseCode = "200", description = "Countries latest stats on covid")
    @ApiResponse(responseCode = "400", description = "Check response for details")
    @GetMapping(ServiceConstant.API_VERSION + "/countries/{country}")
    public ResponseEntity<CovidStatResponse> getCountryStats(@PathVariable ("country") Country country,
                                                             @RequestParam (value = "referencedDate", required = false, defaultValue = "")
                                                             @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate referencedDate
                                                             ){
        LocalDate effectiveDate = (referencedDate != null) ? referencedDate : LocalDate.now();

        return ResponseEntity.status(HttpStatus.OK).body(orchestrator.fetchStats(country, effectiveDate));
    }

    @ApiResponse(responseCode = "200", description = "Comparison stats based on global stats on the referenced date")
    @ApiResponse(responseCode = "400", description = "Check response for details")
    @GetMapping(ServiceConstant.API_VERSION + "/countries/compare")
    public ResponseEntity<List<CovidStatResponse>> getCountryComparisonStats(
            @RequestParam (value = "referencedDate", required = false, defaultValue = "")
            @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate referencedDate,
            @RequestParam (value = "country1", required = false, defaultValue = "") Country country1,
            @RequestParam (value = "country2", required = false, defaultValue = "") Country country2,
            @RequestParam (value = "country3", required = false, defaultValue = "") Country country3,
            @RequestParam (value = "country4", required = false, defaultValue = "") Country country4

    ){
        Country[] countries = new Country[] {country1, country2, country3, country4};
        LocalDate effectiveDate = (referencedDate != null) ? referencedDate : LocalDate.now();

        return ResponseEntity.status(HttpStatus.OK).body(orchestrator.fetchComparisionStats(countries, effectiveDate));
    }

    @ApiResponse(responseCode = "200", description = "List of all supported countries")
    @ApiResponse(responseCode = "400", description = "Check response for details")
    @GetMapping(ServiceConstant.API_VERSION + "/countries/supportedCountries")
    public ResponseEntity<List<String>> getSupportedCountries(){
        return  ResponseEntity.status(HttpStatus.OK).body(Arrays.stream(Country.values()).map(country -> country.getDisplayName()).collect(Collectors.toList()));
    }




}
