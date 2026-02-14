package com.myreflectionthoughts.covidstat.controller;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.service.Orchestrator;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(ServiceConstant.API_PREFIX)
public class CovidStatController {

    private final Orchestrator orchestrator;

    public CovidStatController(Orchestrator orchestrator){
        this.orchestrator = orchestrator;
    }


    @ApiResponse(responseCode = "200", description = "Countries latest stats on covid")
    @GetMapping(ServiceConstant.API_VERSION + "/countries/{country}")
    public ResponseEntity<CovidStatResponse> getCountryStats(@PathVariable ("country") String country,
                                                             @RequestParam (value = "referencedDate", required = false, defaultValue = "") String referencedDate
                                                             ){
        return ResponseEntity.status(HttpStatus.OK).body(orchestrator.fetchStats(country, referencedDate));
    }

    @ApiResponse(responseCode = "200", description = "Comparison stats based on global stats on the referenced date")
    @GetMapping(ServiceConstant.API_VERSION + "/countries/compare")
    public ResponseEntity<List<CovidStatResponse>> getCountryComparisonStats(
            @RequestParam (value = "referencedDate", required = false, defaultValue = "") String referencedDate,
            @RequestParam (value = "country1", required = false, defaultValue = "") String country1,
            @RequestParam (value = "country2", required = false, defaultValue = "") String country2,
            @RequestParam (value = "country3", required = false, defaultValue = "") String country3,
            @RequestParam (value = "country4", required = false, defaultValue = "") String country4

    ){
        String[] countries = new String[] {country1, country2, country3, country4};
        return ResponseEntity.status(HttpStatus.OK).body(orchestrator.fetchComparisionStats(countries, referencedDate));
    }


}
