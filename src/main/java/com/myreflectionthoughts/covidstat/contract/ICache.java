package com.myreflectionthoughts.covidstat.contract;

public interface ICache<K, V>{

    void init();
    void ping();
    void put(K key, V value, long expiryTimestamp);
    V get(K key);
}
