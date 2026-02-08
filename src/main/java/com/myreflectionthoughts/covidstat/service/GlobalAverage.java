package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.entity.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;

public class GlobalAverage implements ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> {
    @Override
    public ResponseWrapper calculate(ExternalAPIResponse data) {
        return null;
    }
}
