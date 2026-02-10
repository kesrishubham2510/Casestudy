package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingUtility {

    private final ObjectMapper objectMapper;

    private MappingUtility() {
        this.objectMapper = new ObjectMapper();
    }

    public <T> T parseToPOJO(String response, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(response, t);
    }

    private static class MappingUtilityInstance{
        private static final MappingUtility mappingUtility = new MappingUtility();
    }

    public static MappingUtility getMappingUtilityInstance(){
        return MappingUtilityInstance.mappingUtility;
    }
}
