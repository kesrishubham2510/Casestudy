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
}
