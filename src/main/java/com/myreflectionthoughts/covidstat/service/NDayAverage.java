package com.myreflectionthoughts.covidstat.service;

import com.myreflectionthoughts.covidstat.contract.ITrendEvaluation;
import com.myreflectionthoughts.covidstat.entity.Trend;
import com.myreflectionthoughts.covidstat.entity.Trends;
import com.myreflectionthoughts.covidstat.entity.externaldto.CoverageStatTimeline;
import com.myreflectionthoughts.covidstat.entity.externaldto.ExternalAPIResponse;
import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class NDayAverage implements ITrendEvaluation<ExternalAPIResponse, ResponseWrapper> {

    private NDayAverage(){}

    @Override
    public Trends calculate(ExternalAPIResponse data, int[] days) {

        int nDay;

        nDay = 0;
        Trends trends = new Trends();

        for(; nDay < days.length; nDay++){
            trends.getTrends().add(calculateTrend(data.getTimeline().subList(0, days[nDay])));
        }

        return trends;
    }

    private Trend calculateTrend(List<CoverageStatTimeline> stats){

        Trend trend = new Trend();

        int itr, size;
        long startingValue, lastValue;
        double change;
        BigDecimal dailyCases;

        itr=0;
        dailyCases = new BigDecimal(0);
        size = stats.size();

        startingValue = Long.parseLong(stats.get(0).getTotal());
        lastValue = Long.parseLong(stats.get(size-1).getTotal());
        change = startingValue == 0 ? 0 : ((0.0 + lastValue - startingValue)/startingValue)*100;

        trend.setChangePercentage(change);

        for(; itr < size; itr++){
           dailyCases = dailyCases.add(new BigDecimal(Long.parseLong(stats.get(itr).getDaily())));
        }

        dailyCases = dailyCases.divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP);
        trend.setDailyAverage(dailyCases.toPlainString());

        if(dailyCases.compareTo(BigDecimal.ZERO) > 0){
            trend.setAlertMessage("Situation Worsening");
        }else if(dailyCases.compareTo(BigDecimal.ZERO) == 0){
            trend.setAlertMessage("Stable");
        }else{
            trend.setAlertMessage("Improving");
        }

        if(change > 10){
            trend.setDirection("UP");
        }else{
            trend.setDirection("DOWN");
        }

        return trend;
    }

    private static class NDayAverageInstance{
        private static final NDayAverage nDayAverage = new NDayAverage();
    }
    public static NDayAverage getNDayAverageInstance(){
        return NDayAverageInstance.nDayAverage;
    }

}
