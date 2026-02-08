package com.myreflectionthoughts.covidstat.registry;

import com.myreflectionthoughts.covidstat.enums.USECASE;

import java.util.Arrays;
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
       map.put(USECASE.LATEST_STAT.name(), "");
       map.put(USECASE.VACCINE_COVERAGE.name(), "");
    }
}
