package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NDayAverageTest {

    private final NDayAverage nDayAverage;

    public NDayAverageTest(){
        this.nDayAverage = new NDayAverage();
    }
    @Test
    public void testCalculateNDayGlobalTrend(){
        ExternalAPIResponse response = prepareGlobalVaccineCoverageResponse();
        Trends calculatedTrends = this.nDayAverage.calculate(response, new int[] {7, 14, 20});
        assertEquals(3, calculatedTrends.getTrends().size());
    }

    @Test
    public void testCalculateNDayIndiaTrend(){
        ExternalAPIResponse response = prepareVaccineCoverage_India_Response();
        Trends calculatedTrends = this.nDayAverage.calculate(response, new int[] {7, 14, 20});
        assertEquals(3, calculatedTrends.getTrends().size());
    }

    private ExternalAPIResponse prepareGlobalVaccineCoverageResponse(){
        return TestDataUtility.getGlobalVaccineCoverageData("VaccineCoverage_Global_Last_25_Days");
    }

    private ExternalAPIResponse prepareVaccineCoverage_India_Response(){
        return TestDataUtility.getVaccineCoverage_India_Data("VaccineCoverage_India_Last_25_Days");
    }
}
