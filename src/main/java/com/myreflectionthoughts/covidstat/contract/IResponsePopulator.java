package com.myreflectionthoughts.covidstat.contract;

public interface IResponsePopulator<in, out> {
    out populate(in input1, in input2);
}