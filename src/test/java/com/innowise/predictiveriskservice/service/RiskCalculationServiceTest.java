package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.config.RiskWeightConfig;
import com.innowise.predictiveriskservice.entity.RiskScoreCalculationData;
import com.innowise.predictiveriskservice.exception.IllegalRiskScoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;

class RiskCalculationServiceTest {

    private RiskWeightConfig riskWeightConfig;
    private RiskCalculationService riskCalculationService;

    @BeforeEach
    void setUp() {
        riskWeightConfig = new RiskWeightConfig();
        riskCalculationService = new RiskCalculationService(riskWeightConfig);
    }

    private RiskScoreCalculationData data(double delay, double weather, double vendorHistory, double documentErrors) {
        return new RiskScoreCalculationData(UUID.randomUUID(), delay, weather, vendorHistory, documentErrors);
    }

    @Test
    @DisplayName("Computes the weighted sum of the four risk factors")
    void calculateRiskScore_validInput_returnsWeightedSum() {
        riskWeightConfig.setDelay(0.1);
        riskWeightConfig.setWeather(0.1);
        riskWeightConfig.setVendorHistory(0.1);
        riskWeightConfig.setDocumentErrors(0.1);

        double score = riskCalculationService.calculateRiskScore(data(1.0, 1.0, 1.0, 2.0));

        assertThat(score).isCloseTo(0.35, offset(1e-9));
    }

    @Test
    @DisplayName("Accepts the lower boundary score of exactly 0.0")
    void calculateRiskScore_boundaryMin_doesNotThrow() {
        riskWeightConfig.setDelay(0.0);
        riskWeightConfig.setWeather(0.0);
        riskWeightConfig.setVendorHistory(0.0);
        riskWeightConfig.setDocumentErrors(0.0);

        double score = riskCalculationService.calculateRiskScore(data(1.0, 1.0, 1.0, 1.0));

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Accepts the upper boundary score of exactly 1.0")
    void calculateRiskScore_boundaryMax_doesNotThrow() {
        riskWeightConfig.setDelay(0.25);
        riskWeightConfig.setWeather(0.25);
        riskWeightConfig.setVendorHistory(0.25);
        riskWeightConfig.setDocumentErrors(0.25);

        double score = riskCalculationService.calculateRiskScore(data(1.0, 1.0, 1.0, 1.0));

        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Throws when the computed score exceeds 1.0")
    void calculateRiskScore_scoreAboveMax_throwsIllegalRiskScoreException() {
        riskWeightConfig.setDelay(1.0);
        riskWeightConfig.setWeather(1.0);
        riskWeightConfig.setVendorHistory(1.0);
        riskWeightConfig.setDocumentErrors(1.0);

        assertThatThrownBy(() -> riskCalculationService.calculateRiskScore(data(1.0, 1.0, 1.0, 1.0)))
                .isInstanceOf(IllegalRiskScoreException.class)
                .hasMessageContaining("Risk Score is not between 0.0 and 1.0");
    }

    @Test
    @DisplayName("Throws when the computed score is negative")
    void calculateRiskScore_scoreBelowMin_throwsIllegalRiskScoreException() {
        riskWeightConfig.setDelay(-1.0);
        riskWeightConfig.setWeather(0.0);
        riskWeightConfig.setVendorHistory(0.0);
        riskWeightConfig.setDocumentErrors(0.0);

        assertThatThrownBy(() -> riskCalculationService.calculateRiskScore(data(1.0, 1.0, 1.0, 1.0)))
                .isInstanceOf(IllegalRiskScoreException.class);
    }

    @Test
    @DisplayName("A zero-valued factor produces Infinity via division, which is rejected as out of range")
    void calculateRiskScore_zeroDivisor_throwsIllegalRiskScoreException() {
        riskWeightConfig.setDelay(0.1);
        riskWeightConfig.setWeather(0.1);
        riskWeightConfig.setVendorHistory(0.1);
        riskWeightConfig.setDocumentErrors(0.1);

        assertThatThrownBy(() -> riskCalculationService.calculateRiskScore(data(0.0, 1.0, 1.0, 1.0)))
                .isInstanceOf(IllegalRiskScoreException.class);
    }
}