package com.myreflectionthoughts.covidstat.contract;

public interface IDataSource<T> {

    T getLatestStats(String country, long referencedDate);
    T getVaccineCoverage(String country, long referencedDate);
    T getDataForAlerts(String country, long referencedDate);
}
