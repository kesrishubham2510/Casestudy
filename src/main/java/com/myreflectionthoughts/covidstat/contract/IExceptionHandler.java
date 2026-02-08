package com.myreflectionthoughts.covidstat.contract;

public interface IExceptionHandler {

    <K,T> T handleException(K input);
}
