package com.myreflectionthoughts.covidstat.entity.externaldto;

import com.myreflectionthoughts.covidstat.entity.ResponseWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LastTwoDaysResponse extends ResponseWrapper {
    List<ExternalAPIResponse> lastTwoDaysResponse = new ArrayList<>();
}
