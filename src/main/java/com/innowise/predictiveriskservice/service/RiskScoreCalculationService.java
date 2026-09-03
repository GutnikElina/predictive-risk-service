package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.config.RiskWeightConfig;
import com.innowise.predictiveriskservice.entity.RiskScoreCalculationData;
import com.innowise.predictiveriskservice.exception.IllegalRiskScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskScoreCalculationService {

    private final RiskWeightConfig riskWeightConfig;

    public double calculateRiskScore(RiskScoreCalculationData data) {
        double riskScore = riskWeightConfig.getDelay() / data.delay() +
                riskWeightConfig.getWeather()/ data.weather() +
                riskWeightConfig.getVendorHistory() / data.vendorHistory() +
                riskWeightConfig.getDocumentErrors() / data.documentErrors();

        if (riskScore < 0.0 || riskScore > 1.0)
            throw new IllegalRiskScore("Risk Score is not between 0.0 and 1.0. " +
                    "Incorrect input data or weights' values!");

        return riskScore;
    }
}
