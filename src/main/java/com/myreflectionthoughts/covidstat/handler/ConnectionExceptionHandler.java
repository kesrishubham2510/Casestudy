package com.myreflectionthoughts.covidstat.handler;

import com.myreflectionthoughts.covidstat.contract.IExceptionHandler;

// As, I'm planning to log the exception and serve cached response to client
public class ConnectionExceptionHandler implements IExceptionHandler<String, Void> {
    @Override
    public Void handleException(String input) {

        return null;
    }
}
