package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestDataUtility {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertTOPOJO(String content, Class<T> t) {
        try {
            return objectMapper.readValue(content, t);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileContent(String filePath){

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
    public static <T> T getDefaultResponse(String path, Class<T> t){
        String content = getFileContent(path);
        return convertTOPOJO(content, t);
    }
}
