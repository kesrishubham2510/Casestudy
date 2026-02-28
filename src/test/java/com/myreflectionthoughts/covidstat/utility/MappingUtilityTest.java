package com.myreflectionthoughts.covidstat.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class MappingUtilityTest {

    static class TestDto {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private MappingUtility mappingUtility;

    public MappingUtilityTest() {
        mappingUtility = MappingUtility.getMappingUtilityInstance();
    }

    @Test
    public void testSingletonInstance() {
        MappingUtility instance1 = MappingUtility.getMappingUtilityInstance();
        MappingUtility instance2 = MappingUtility.getMappingUtilityInstance();
        assertEquals(instance1, instance2);
    }

    @Test
    public void testParseToPOJO_ValidJson() throws JsonProcessingException {
        String json = "{\"name\":\"CovidStat\"}";
        TestDto result = mappingUtility.parseToPOJO(json, TestDto.class);

        assertNotNull(result);
        assertEquals("CovidStat", result.getName());
    }

    @Test
    public void testParseToPOJO_InvalidJson_ThrowsException() throws JsonProcessingException {
        String invalidJson = "{\"name\":"; // Malformed JSON
        assertThrows(JsonEOFException.class, ()->  mappingUtility.parseToPOJO(invalidJson, TestDto.class));
    }

    @Test
    public void testAdjustGlobalVaccineCoverageResponse() throws JsonProcessingException {
        String apiResponseArray = "[{\"date\":\"2023-01-01\",\"value\":100}]";

        String result = MappingUtility.adjustGlobalVaccineCoverageResponse(apiResponseArray);

        assertTrue(result.contains("\"timeline\""));
        assertTrue(result.contains("2023-01-01"));
    }

    @Test
    public void testConvertToJsonStructure_ValidObject() {
        TestDto dto = new TestDto();
        dto.setName("India");

        String result = MappingUtility.convertToJsonStructure(dto);

        assertEquals("{\"name\":\"India\"}", result);
    }

    @Test
    public void testConvertToJsonStructure_HandlesException() {
        Object circular = new Object() {
            public Object self = this;
        };

        String result = MappingUtility.convertToJsonStructure(circular);
        assertEquals("", result);
    }
}
