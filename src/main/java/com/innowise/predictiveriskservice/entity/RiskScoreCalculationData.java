package com.innowise.predictiveriskservice.entity;

public record RiskScoreCalculationData(
        double delay,
        double weather,
        double vendorHistory,
        double documentErrors
) {}
