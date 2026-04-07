package com.myreflectionthoughts.covidstat.contract;

public interface ResponsePopulator<in, out> {
    out populate(in input);
}

