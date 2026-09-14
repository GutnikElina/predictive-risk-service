package com.innowise.predictiveriskservice.entity;

import java.util.UUID;

public record RiskScoreCalculationData(
        UUID orderId,
        double delay,
        double weather,
        double vendorHistory,
        double documentErrors
) {}
