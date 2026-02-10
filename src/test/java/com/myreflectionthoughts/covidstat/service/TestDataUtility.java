package com.myreflectionthoughts.covidstat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.apache.logging.log4j.util.LoaderUtil.getClassLoader;

public class TestDataUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ExternalAPIResponse getGlobalVaccineCoverageData(String path){
        String fileContent;
        ExternalAPIResponse apiResponse;

        try {

            fileContent = getFileContent("data/"+path + ".json");

            JsonNode array = objectMapper.readTree(fileContent);
            ObjectNode key = objectMapper.createObjectNode();
            key.set("timeline", array);

            fileContent = objectMapper.writeValueAsString(key);
            apiResponse = objectMapper.readValue(fileContent, ExternalAPIResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return apiResponse;
    }

    public static ExternalAPIResponse getVaccineCoverage_India_Data(String path){
        String fileContent;
        ExternalAPIResponse apiResponse;

        try {

            fileContent = getFileContent("data/"+path + ".json");
            apiResponse = objectMapper.readValue(fileContent, ExternalAPIResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return apiResponse;
    }

    private static String getFileContent(String filePath){

        try {
            InputStream is = TestDataUtility.class
                    .getClassLoader()
                    .getResourceAsStream(filePath);

            if (is == null) {
                throw new RuntimeException("File not found: " + filePath);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
