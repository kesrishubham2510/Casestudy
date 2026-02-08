package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IRemoteConnection;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.registry.URLTemplateRegistry;

import java.util.logging.Logger;

public class RemoteDataSource implements IDataSource<ExternalAPIResponse> {

    private final Logger logger;
    private final URLTemplateRegistry urlTemplateRegistry;
    private final IRemoteConnection remoteConnection;

    RemoteDataSource(URLTemplateRegistry urlTemplateRegistry, IRemoteConnection remoteConnection){
        this.logger = Logger.getLogger(RemoteDataSource.class.getSimpleName());
        this.urlTemplateRegistry = new URLTemplateRegistry();
        this.remoteConnection = remoteConnection;
    }

    @Override
    public ExternalAPIResponse getLatestStats(String country, long referencedDate) {
        return null;
    }

    @Override
    public ExternalAPIResponse getVaccineCoverage(String country, long referencedDate) {
        return null;
    }

    @Override
    public ExternalAPIResponse getDataForAlerts(String country, long referencedDate) {
        return null;
    }
}
