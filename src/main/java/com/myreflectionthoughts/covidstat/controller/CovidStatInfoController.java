package com.myreflectionthoughts.covidstat.controller;

import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value= ServiceConstant.API_PREFIX)
public class CovidStatInfoController {

    @GetMapping(value = "/health")
    public ResponseEntity<String> getHealthStatus(){
       return ResponseEntity.ok("Health is fine");
    }

    @GetMapping(value = "/info")
    public ResponseEntity<String> getBuildInfo(){
        return ResponseEntity.ok("Build is fine");
    }
}
