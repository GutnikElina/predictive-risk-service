package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.config.RiskWeightConfig;
import com.innowise.predictiveriskservice.entity.RiskScoreCalculationData;
import com.innowise.predictiveriskservice.exception.IllegalRiskScoreException;
import com.innowise.predictiveriskservice.kafka.avro.RiskLevelEscalatedEvent;
import com.innowise.predictiveriskservice.outbox.EventType;
import com.innowise.predictiveriskservice.outbox.OutboxEvent;
import com.innowise.predictiveriskservice.outbox.OutboxEventRepository;
import com.innowise.predictiveriskservice.outbox.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiskScoreCalculationService {

    private final RiskWeightConfig riskWeightConfig;

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    private static final double MIN_RISK_SCORE = 0.0;
    private static final double MAX_RISK_SCORE = 1.0;
    private static final double ESCALATION_THRESHOLD = 0.6;

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

    public void createEventIfRiskLevelEscalated(Double riskScore) {
        if (riskScore > ESCALATION_THRESHOLD) {
            RiskLevelEscalatedEvent event = new RiskLevelEscalatedEvent(
                    UUID.randomUUID(),
                    riskScore,
                    "", // right now there is no info of how we receive / calculate it
                    ""         // same
            );

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(EventType.RISK_LEVEL_ESCALATED_EVENT)
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxStatus.PENDING)
                    .attemptCount(0)
                    .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                    .updatedAt(Instant.now().atOffset(ZoneOffset.UTC))
                    .build();

            outboxEventRepository.save(outboxEvent);
        }
    }
}
