package com.myreflectionthoughts.covidstat.handler;

import com.myreflectionthoughts.covidstat.contract.IExceptionHandler;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;

// As, I'm planning to log the exception and serve cached response to client
public class BadRequestExceptionHandler implements IExceptionHandler<CaseStudyException, Void> {

    @Override
    public Void handleException(CaseStudyException exception) {
        return null;
    }
}
