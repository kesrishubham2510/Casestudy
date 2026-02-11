package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class MappingUtility {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    private MappingUtility() {
    }

    public <T> T parseToPOJO(String response, Class<T> t) throws JsonProcessingException {
        return objectMapper.readValue(response, t);
    }

    public static String adjustGlobalVaccineCoverageResponse(String apiResponse) throws JsonProcessingException {

        JsonNode array = objectMapper.readTree(apiResponse);
        ObjectNode key = objectMapper.createObjectNode();
        key.set("timeline", array);

        return objectMapper.writeValueAsString(key);
    }

    public static String convertToJsonStructure(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            // TODO: Add a logger
            return "";
        }
    }

    private static class MappingUtilityInstance{
        private static final MappingUtility mappingUtility = new MappingUtility();
    }

    public static MappingUtility getMappingUtilityInstance(){
        return MappingUtilityInstance.mappingUtility;
    }
}
