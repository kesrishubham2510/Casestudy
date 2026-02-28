package com.myreflectionthoughts.covidstat.utility;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

 class DataUtilityTest {

    @Test
     void testConvertTOPOJO_Success() {
        String json = "{\"name\":\"India\", \"cases\":5000}";
        Map<String, Object> result = DataUtility.convertTOPOJO(json, Map.class);

        assertNotNull(result);
        assertEquals("India", result.get("name"));
        assertEquals(5000, result.get("cases"));
    }


    @Test
     void testConvertTOPOJO_InvalidJson_ThrowsException() {
        String malformedJson = "{ name: India }"; // Missing quotes
        assertThrows(RuntimeException.class, ()-> DataUtility.convertTOPOJO(malformedJson, Map.class));
    }

    @Test
     void testGetFileContent_Success() {
        String content = DataUtility.getFileContent("data/StaticCovidStatResponse.json");

        assertNotNull(content);
        assertTrue(content.contains("country"));
        assertTrue(content.contains("noOfRecoveries"));
    }


    @Test
     void testGetFileContent_FileNotFound_ThrowsException() {
       assertThrows(RuntimeException.class, ()->  DataUtility.getFileContent("non-existent-file.json"));
    }


    @Test
     void testFileToPOJO_Integration() {
        String content = DataUtility.getFileContent("data/StaticCovidStatResponse.json");
        Map<String, Object> result = DataUtility.convertTOPOJO(content, Map.class);

        assertEquals("India", result.get("country"));
    }
}
