package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.constant.Country;
import com.myreflectionthoughts.covidstat.contract.IValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CountryInputValidator implements IValidator<Country> {
    @Override
    public boolean isValid(Country country) {
        return Arrays.stream(Country.values())
                .anyMatch(existingCountry -> existingCountry.equals(country));
    }
}
