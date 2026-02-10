package com.myreflectionthoughts.covidstat;

import com.myreflectionthoughts.covidstat.service.RedisCacheService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CovidstatApplication {

	public static void main(String[] args) {
		RedisCacheService.getRedisCacheInstance();
		SpringApplication.run(CovidstatApplication.class, args);
	}

}
