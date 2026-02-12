package com.myreflectionthoughts.covidstat.handler;

import com.myreflectionthoughts.covidstat.contract.IExceptionHandler;
import com.myreflectionthoughts.covidstat.exception.CaseStudyException;

public class GenericExceptionHandler implements IExceptionHandler<CaseStudyException, Void> {
    @Override
    public Void handleException(CaseStudyException exception) {
        return null;
    }
}
