package com.myreflectionthoughts.covidstat.service.impl.populator;

import com.myreflectionthoughts.covidstat.contract.ICacheFacade;
import com.myreflectionthoughts.covidstat.contract.IDataSource;
import com.myreflectionthoughts.covidstat.contract.IResponsePopulator;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.externaldto.LastTwoDaysResponse;
import com.myreflectionthoughts.covidstat.utility.CacheUtility;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Component
public class AlertMessagePopulator implements IResponsePopulator<String, String> {

    private final ICacheFacade<String, String> redisAlertMessagePopulatorService;
    private final ICacheFacade<String, ExternalAPIResponse> redisCacheExternalAPIResponseService;
    private final IDataSource<ResponseWrapper> remoteDataSource;

    private static final Logger logger = Logger.getLogger(AlertMessagePopulator.class.getSimpleName());

    public AlertMessagePopulator( ICacheFacade<String, String> redisAlertMessagePopulatorService,
                                  ICacheFacade<String, ExternalAPIResponse> redisCacheExternalAPIResponseService,
                                    IDataSource<ResponseWrapper> remoteDataSource){
        this.redisAlertMessagePopulatorService = redisAlertMessagePopulatorService;
        this.redisCacheExternalAPIResponseService = redisCacheExternalAPIResponseService;
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public String populate(String country, String referencedDate) {

        String alertMessageKey = CacheUtility.getKeyForAlertMessage(country, referencedDate);
        String alertMessage = redisAlertMessagePopulatorService.get(alertMessageKey);

        if (Objects.isNull(alertMessage)) {

            logger.info("Pre-computed alert message not found:- "+country+", referencedDate:- "+referencedDate);

            LastTwoDaysResponse lastTwoDaysResponse = (LastTwoDaysResponse) remoteDataSource.getDataForAlerts(country, 0L);

            String cacheKeyForExternalAPIResponse = CacheUtility.getKeyForRawAPIResponseForCurrentStat(country);
            ExternalAPIResponse externalAPIResponse = redisCacheExternalAPIResponseService.get(cacheKeyForExternalAPIResponse);

            if(Objects.isNull(externalAPIResponse)){
                externalAPIResponse = (ExternalAPIResponse) remoteDataSource.getLatestStats(country, 0L);
                externalAPIResponse.setCountry(country);
            }

            lastTwoDaysResponse.getLastTwoDaysResponse().add(externalAPIResponse);
            alertMessage = evaluateAlertMessage(lastTwoDaysResponse);

            redisAlertMessagePopulatorService.put(alertMessageKey+"_"+alertMessage);
        }

        return alertMessageKey;
    }

    private String evaluateAlertMessage(LastTwoDaysResponse lastTwoDaysResponse){

        String alertMessage = "";
        List<ExternalAPIResponse> apiResponses = lastTwoDaysResponse.getLastTwoDaysResponse();

        double avgPrevious =
                (apiResponses.get(0).getTodayCases() + apiResponses.get(1).getTodayCases()) / 2.0;

        double percentIncrease =
                ((apiResponses.get(2).getTodayCases() - avgPrevious) / avgPrevious) * 100;


        if (percentIncrease > 20) {
            alertMessage = "Rising cases - Follow safety protocols";
        }

        return alertMessage;
    }

}
