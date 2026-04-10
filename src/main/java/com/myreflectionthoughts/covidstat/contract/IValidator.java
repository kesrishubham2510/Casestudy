package com.myreflectionthoughts.covidstat.contract;

public interface IValidator<T>{
    boolean isValid(T t);
}
