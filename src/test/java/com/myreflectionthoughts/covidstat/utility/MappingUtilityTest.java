package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

 class MappingUtilityTest {

    static class TestDto {
        public String name;
         String getName() { return name; }
         void setName(String name) { this.name = name; }
    }

    private MappingUtility mappingUtility;

     MappingUtilityTest() {
        mappingUtility = MappingUtility.getMappingUtilityInstance();
    }

    @Test
     void testSingletonInstance() {
        MappingUtility instance1 = MappingUtility.getMappingUtilityInstance();
        MappingUtility instance2 = MappingUtility.getMappingUtilityInstance();
        assertEquals(instance1, instance2);
    }

    @Test
     void testParseToPOJO_ValidJson() throws JsonProcessingException {
        String json = "{\"name\":\"CovidStat\"}";
        TestDto result = mappingUtility.parseToPOJO(json, TestDto.class);

        assertNotNull(result);
        assertEquals("CovidStat", result.getName());
    }

    @Test
     void testParseToPOJO_InvalidJson_ThrowsException() throws JsonProcessingException {
        String invalidJson = "{\"name\":"; // Malformed JSON
        assertThrows(JsonEOFException.class, ()->  mappingUtility.parseToPOJO(invalidJson, TestDto.class));
    }

    @Test
     void testAdjustGlobalVaccineCoverageResponse() throws JsonProcessingException {
        String apiResponseArray = "[{\"date\":\"2023-01-01\",\"value\":100}]";

        String result = MappingUtility.adjustGlobalVaccineCoverageResponse(apiResponseArray);

        assertTrue(result.contains("\"timeline\""));
        assertTrue(result.contains("2023-01-01"));
    }

    @Test
     void testConvertToJsonStructure_ValidObject() {
        TestDto dto = new TestDto();
        dto.setName("India");

        String result = MappingUtility.convertToJsonStructure(dto);

        assertEquals("{\"name\":\"India\"}", result);
    }

    @Test
     void testConvertToJsonStructure_HandlesException() {
        Object circular = new Object() {
             Object self = this;
        };

        String result = MappingUtility.convertToJsonStructure(circular);
        assertEquals("", result);
    }
}
