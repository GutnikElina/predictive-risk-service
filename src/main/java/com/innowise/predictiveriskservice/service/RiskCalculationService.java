package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.config.RiskWeightConfig;
import com.innowise.predictiveriskservice.entity.RiskScoreCalculationData;
import com.innowise.predictiveriskservice.exception.IllegalRiskScoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskCalculationService {

    private final RiskWeightConfig riskWeightConfig;

    private static final double MIN_RISK_SCORE = 0.0;
    private static final double MAX_RISK_SCORE = 1.0;

    public double calculateRiskScore(RiskScoreCalculationData data) {
        double riskScore = riskWeightConfig.getDelay() / data.delay()
                + riskWeightConfig.getWeather() / data.weather()
                + riskWeightConfig.getVendorHistory() / data.vendorHistory()
                + riskWeightConfig.getDocumentErrors() / data.documentErrors();

        if (riskScore < MIN_RISK_SCORE || riskScore > MAX_RISK_SCORE)
            throw new IllegalRiskScoreException("Risk Score is not between 0.0 and 1.0. " +
                    "Incorrect input data or weights' values!");

        return riskScore;
    }
}
