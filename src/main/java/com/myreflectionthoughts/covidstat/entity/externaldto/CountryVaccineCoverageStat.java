package com.myreflectionthoughts.covidstat.entity.externaldto;

import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;

import java.util.List;

public class CountryVaccineCoverageStat extends ResponseWrapper {
    private String country;
    private List<CoverageStatTimeline> timeline;
}
