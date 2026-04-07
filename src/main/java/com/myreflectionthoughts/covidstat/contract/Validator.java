package com.myreflectionthoughts.covidstat.contract;

public interface Validator<T>{
    boolean isValid(T t);
}
