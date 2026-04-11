package com.myreflectionthoughts.covidstat.service.impl.populator;

import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IResponsePopulator;
import com.myreflectionthoughts.covidstat.entity.CovidStatResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Logger;

@Component
public class CountryStatResponsePopulator implements IResponsePopulator<String, CovidStatResponse> {

    private final ICacheFacade<String, ExternalAPIResponse> redisCacheExternalAPIResponseService;
    private final IDataSource<ResponseWrapper> remoteDataSource;

    private static final Logger logger = Logger.getLogger(CountryStatResponsePopulator.class.getSimpleName());

    public CountryStatResponsePopulator( ICacheFacade<String, ExternalAPIResponse> redisCacheExternalAPIResponseService,
                                         IDataSource<ResponseWrapper> remoteDataSource
                                         ){
        this.redisCacheExternalAPIResponseService = redisCacheExternalAPIResponseService;
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public CovidStatResponse populate(String country, String referencedDate) {
        CovidStatResponse covidStatResponse = new CovidStatResponse();

        if(StringUtils.isBlank(referencedDate)){
            referencedDate = "02-02-2021";
        }

        String cacheKeyForLatestStat = CacheUtility.getKeyForRawAPIResponseForCurrentStat(country);

        ExternalAPIResponse externalAPIResponse = redisCacheExternalAPIResponseService.get(cacheKeyForLatestStat);

        if (Objects.nonNull(externalAPIResponse)) {

            logger.info("Found latest data in the cache");
        }else{

            externalAPIResponse = (ExternalAPIResponse) remoteDataSource.getLatestStats(country, 0L);
            externalAPIResponse.setCountry(country);

            // Keeping TTL as 35 minutes, as every 30 minutes new data s pushed into the API
            redisCacheExternalAPIResponseService.put(externalAPIResponse);
        }

        covidStatResponse.setCountry(country);
        covidStatResponse.setActiveAsToday(String.valueOf(externalAPIResponse.getActive()));
        covidStatResponse.setNoOfCases(String.valueOf(externalAPIResponse.getCases()));
        covidStatResponse.setNoOfRecoveries(String.valueOf(externalAPIResponse.getRecovered()));
        covidStatResponse.setReferencedDate(referencedDate);
        return  covidStatResponse;
    }
}
