package com.myreflectionthoughts.covidstat.contract;

public interface ICache {

    void init();
    void ping();
    <K, V> void put(K key, V value, long expiryTimestamp);
    <K, V> V get(K key);
}
