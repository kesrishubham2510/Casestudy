package com.myreflectionthoughts.covidstat.contract;

public interface IDataSource {

    <T> T getLatestStats(String country, long referencedDate);
    <T> T getVaccineCoverage(String country, long referencedDate);
}
