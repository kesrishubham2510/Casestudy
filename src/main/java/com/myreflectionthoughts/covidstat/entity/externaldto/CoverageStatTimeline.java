package com.myreflectionthoughts.covidstat.entity.externaldto;

import lombok.Data;

@Data
public class CoverageStatTimeline {
     private String total;
     private String daily;
     private String totalPerHundred;
     private String dailyPerMillion;
     private String date;
}
