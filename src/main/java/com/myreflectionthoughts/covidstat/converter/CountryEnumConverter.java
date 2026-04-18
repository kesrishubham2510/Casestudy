package com.myreflectionthoughts.covidstat.converter;

import com.myreflectionthoughts.covidstat.constant.Country;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class CountryEnumConverter implements Converter<String, Country> {

    @Override
    public Country convert(String source) {
        return Country.valueOf(source.toUpperCase());
    }
}
