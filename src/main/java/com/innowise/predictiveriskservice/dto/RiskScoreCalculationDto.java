package com.innowise.predictiveriskservice.dto;

import java.util.UUID;

public record RiskScoreCalculationDto(
        UUID orderId,
        double delay,
        double weather,
        double vendorHistory,
        double documentErrors
) {}
