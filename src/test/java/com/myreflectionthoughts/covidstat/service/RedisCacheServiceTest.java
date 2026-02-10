package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RedisCacheServiceTest {

    private final RedisCacheService redisCacheService;

    public RedisCacheServiceTest() {
        this.redisCacheService = RedisCacheService.getRedisCacheInstance();
    }

//    @Test
    public void testPing(){}

//    @Test
    public void testPutAndGetMethod(){
        String key = "this::is::the::key";
        ExternalAPIResponse externalAPIResponse = TestDataUtility.getGlobalVaccineCoverageData("VaccineCoverage_Global_Last_25_Days");
        ZonedDateTime now = ZonedDateTime.now();

        // 2. Truncate to the start of the current day (midnight today).
        ZonedDateTime midnightToday = now.toLocalDate().atStartOfDay(now.getZone());

        // 3. Add one day to get midnight tomorrow.
        ZonedDateTime midnightTomorrow = midnightToday.plusDays(1);

        // 4. Convert to an Instant (a point on the universal timeline) and get the Unix timestamp (seconds).
        long unixTimestampSeconds = midnightTomorrow.toInstant().getEpochSecond();

        redisCacheService.put(key, String.valueOf(externalAPIResponse), unixTimestampSeconds);

        String cachedResponse = redisCacheService.get(key);
        assertNotNull(cachedResponse);
        assertNotNull(TestDataUtility.convertTOPOJO(cachedResponse, ExternalAPIResponse.class));
    }

}
