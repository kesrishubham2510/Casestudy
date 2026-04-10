package com.myreflectionthoughts.covidstat.contract;

public interface ICacheFacade<K, V>{
    void put(V v);
    V get(K k);
}
