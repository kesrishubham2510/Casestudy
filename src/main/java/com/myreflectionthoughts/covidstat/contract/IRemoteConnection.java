package com.myreflectionthoughts.covidstat.contract;

import java.util.Map;

public interface IRemoteConnection<K> {

    K executeGetRequest(String url, Map<String, String> headers);
}
