package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.entity.RiskScoreCalculationData;
import com.innowise.predictiveriskservice.entity.KafkaEventTypes;
import com.innowise.predictiveriskservice.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import com.innowise.predictiveriskservice.kafka.avro.RiskLevelEscalatedEvent;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GpsProcessingService {

    private final StringRedisTemplate redisTemplate;
    private final RiskCalculationService riskCalculationService;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    private static final Duration CALCULATION_INTERVAL = Duration.ofSeconds(60);

    private static final String LOCK_VALUE = "LOCKED";
    private static final String LOCK_KEY_PREFIX = "trip:risk:lock:";

    private static final double ESCALATION_THRESHOLD = 0.6;

    public void processGpsPosition(RiskScoreCalculationData data) {
        String lockKey = LOCK_KEY_PREFIX + data.orderId();

        Boolean isFirstRequestInWindow = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, LOCK_VALUE, CALCULATION_INTERVAL);

        if (Boolean.TRUE.equals(isFirstRequestInWindow)) {
            log.info("Triggering Risk Score recalculation for order {}", data.orderId());

            double riskScore = riskCalculationService.calculateRiskScore(data);

            if (riskScore > ESCALATION_THRESHOLD) {
                RiskLevelEscalatedEvent event = new RiskLevelEscalatedEvent(
                        UUID.randomUUID(),
                        riskScore,
                        "", // right now there is no info of how we receive / calculate it
                        ""         // same
                );

                outboxService.save(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT,
                        objectMapper.writeValueAsString(event));
            }
        }
    }
}
