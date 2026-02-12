package com.myreflectionthoughts.covidstat.entity.externaldto;

import lombok.Data;

// This data tells about only vaccination coverage
@Data
public class CoverageStatTimeline {
     private String total;
     private String daily;
     private String totalPerHundred;
     private String dailyPerMillion;
     private String date;
}
