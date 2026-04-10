package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.config.CacheTTLConfig;
import com.myreflectionthoughts.covidstat.constant.ServiceConstant;
import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IResponsePopulator;
import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import com.myreflectionthoughts.covidstat.utility.DataUtility;
import com.myreflectionthoughts.covidstat.utility.MappingUtility;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Logger;

@Component
public class CovidStatTrendPopulator implements IResponsePopulator<String, CovidStatResponse> {

    private final ICacheFacade<String, HashMap<String, Trends>> redisCacheTrendService;
    private final ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> trendEvaluation;

    private final MappingUtility mappingUtility;
    private final IDataSource<ResponseWrapper> remoteDataSource;


    private static final Logger logger = Logger.getLogger(CovidStatTrendPopulator.class.getSimpleName());;

    public CovidStatTrendPopulator( ICacheFacade<String, HashMap<String, Trends>> redisCacheTrendService,
                                         IDataSource<ResponseWrapper> remoteDataSource){
        this.redisCacheTrendService = redisCacheTrendService;
        this.remoteDataSource = remoteDataSource;
        this.mappingUtility = MappingUtility.getMappingUtilityInstance();
        this.trendEvaluation = NDayAverage.getNDayAverageInstance();
    }

    @Override
    public CovidStatResponse populate(String country, String referencedDate) {

        CovidStatResponse responseWithVaccineCoverage = new CovidStatResponse();
        HashMap<String, Trends> trendsMap = new HashMap<>();

        String cacheKeyForGlobalVaccineCoverageTrends = CacheUtility.getKeyForGlobalVaccineCoverageTrends(referencedDate);
        HashMap<String, Trends> responseForGlobalVaccineCoverageTrends = redisCacheTrendService.get(cacheKeyForGlobalVaccineCoverageTrends);
        String cacheKeyForCountryVaccineCoverageTrends = CacheUtility.getKeyForCountryVaccineCoverageTrends(country, referencedDate);
        HashMap<String, Trends> responseForCountryVaccineCoverageTrends = redisCacheTrendService.get(cacheKeyForCountryVaccineCoverageTrends);

        if (Objects.isNull(responseForGlobalVaccineCoverageTrends) || Objects.isNull(responseForCountryVaccineCoverageTrends)) {

            ExternalAPIResponse countryVaccinationCoverage = null;
            ExternalAPIResponse globalVaccinationCoverage = null;

            // get the vaccine coverage for country
            countryVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage(country, DataUtility.calculateTheDaysBack(referencedDate) + ServiceConstant.MAX_DAY_TREND);
            responseWithVaccineCoverage.setDosesAdministeredInCountry(countryVaccinationCoverage.getTimeline().get(countryVaccinationCoverage.getTimeline().size()-1).getTotal());

            // get the vaccine coverage for global level
            globalVaccinationCoverage = (ExternalAPIResponse) remoteDataSource.getVaccineCoverage("global", DataUtility.calculateTheDaysBack(referencedDate) + ServiceConstant.MAX_DAY_TREND);
            responseWithVaccineCoverage.setDosesAdministeredGlobally(globalVaccinationCoverage.getTimeline().get(countryVaccinationCoverage.getTimeline().size()-1).getTotal());


            Trends countryTrends = (Trends) trendEvaluation.calculate(countryVaccinationCoverage, new int[]{7, 14});
            Trends globalTrends = (Trends) trendEvaluation.calculate(globalVaccinationCoverage, new int[]{7, 14});

            trendsMap.put(country, countryTrends);
            trendsMap.put("global", globalTrends);

            // Keeping the TTL as 120 hours because, a referenced historical trend will never change
            redisCacheTrendService.put(trendsMap);
        }else{
            trendsMap.putAll(responseForGlobalVaccineCoverageTrends);
            trendsMap.putAll(responseForCountryVaccineCoverageTrends);
        }

        responseWithVaccineCoverage.setTrends(trendsMap);
        return  responseWithVaccineCoverage;
    }

}
