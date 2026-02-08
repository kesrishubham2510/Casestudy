package com.myreflectionthoughts.covidstat.contract;

public interface ITrendEvaluation<K,T> {
    T calculate(K data);
}
