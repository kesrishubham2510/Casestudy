package com.myreflectionthoughts.covidstat.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Trends extends ResponseWrapper{
    List<Trend> trends = new ArrayList<>();
}
