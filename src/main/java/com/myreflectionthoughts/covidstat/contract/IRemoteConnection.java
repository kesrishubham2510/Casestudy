package com.myreflectionthoughts.covidstat.contract;

import java.util.Map;

public interface IRemoteConnection {

    <K> K executeRequest(String url, Map<String, String> headers);
}
