package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingUtility {

    private final ObjectMapper objectMapper;

    public MappingUtility() {
        this.objectMapper = new ObjectMapper();
    }

    public <T> T parseToPOJO(String response, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(response, t);
    }
}
