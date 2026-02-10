package com.myreflectionthoughts.covidstat.registry;

import com.myreflectionthoughts.covidstat.enums.USECASE;

import java.util.HashMap;
import java.util.logging.Logger;

public class URLTemplateRegistry {

    private final HashMap<String, String> map;
    private final Logger logger;

    public URLTemplateRegistry(){
        this.map = new HashMap<>();
        intializeMap();
        this.logger = Logger.getLogger(URLTemplateRegistry.class.getSimpleName());
    }

    public String getURL(USECASE usecase){
        return this.map.get(usecase.name());
    }

    private void intializeMap(){
       map.put(USECASE.LATEST_STAT.name(), "/v3/covid-19/countries/{country}?yesterday={yesterday}&twoDaysAgo={twoDaysAgo}&strict={strict}&allowNull={allowNull}");
       map.put(USECASE.VACCINE_COVERAGE.name(), "/v3/covid-19/vaccine/coverage/countries/{country}?lastdays={lastdays}&fullData={fullData}");
    }

    private static class URLTemplateRegistryInstance{
        private static final URLTemplateRegistry urlTemplateRegistry = new URLTemplateRegistry();
    }

    public static URLTemplateRegistry getURLUrlTemplateRegistryInstance(){
        return URLTemplateRegistryInstance.urlTemplateRegistry;
    }
}
