package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.model.enums.KafkaEventTypes;
import com.innowise.predictiveriskservice.dto.RiskScoreCalculationDto;
import com.innowise.predictiveriskservice.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GpsProcessingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private RiskCalculationService riskCalculationService;
    @Mock
    private OutboxService outboxService;
    @Mock
    private ObjectMapper objectMapper;

    private GpsProcessingService gpsProcessingService;

    @BeforeEach
    void setUp() {
        gpsProcessingService = new GpsProcessingService(
                redisTemplate, riskCalculationService, outboxService, objectMapper
        );
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private RiskScoreCalculationDto sampleData() {
        return new RiskScoreCalculationDto(UUID.randomUUID(), 1.0, 1.0, 1.0, 1.0);
    }

    @Test
    void processGpsPosition_firstInWindow_highRisk_savesEscalationEventToOutbox() {
        RiskScoreCalculationDto data = sampleData();
        when(valueOperations.setIfAbsent(eq("trip:risk:lock:" + data.orderId()), eq("LOCKED"), any(Duration.class)))
                .thenReturn(true);
        when(riskCalculationService.calculateRiskScore(data)).thenReturn(0.9);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"riskScore\":0.9}");

        gpsProcessingService.processGpsPosition(data);

        verify(outboxService).save(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT, "{\"riskScore\":0.9}");
    }

    @Test
    void processGpsPosition_firstInWindow_lowRisk_doesNotTouchOutbox() {
        RiskScoreCalculationDto data = sampleData();
        when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);
        when(riskCalculationService.calculateRiskScore(data)).thenReturn(0.2);

        gpsProcessingService.processGpsPosition(data);

        verifyNoInteractions(outboxService);
    }

    @Test
    void processGpsPosition_riskExactlyAtThreshold_doesNotEscalate() {
        RiskScoreCalculationDto data = sampleData();
        when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);
        when(riskCalculationService.calculateRiskScore(data)).thenReturn(0.6);

        gpsProcessingService.processGpsPosition(data);

        verifyNoInteractions(outboxService);
    }

    @Test
    void processGpsPosition_notFirstInWindow_skipsCalculationEntirely() {
        RiskScoreCalculationDto data = sampleData();
        when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(false);

        gpsProcessingService.processGpsPosition(data);

        verifyNoInteractions(riskCalculationService);
        verifyNoInteractions(outboxService);
    }

    @Test
    void processGpsPosition_redisReturnsNull_treatedAsAlreadyLocked_skipsCalculation() {
        RiskScoreCalculationDto data = sampleData();
        when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(null);

        gpsProcessingService.processGpsPosition(data);

        verifyNoInteractions(riskCalculationService);
        verifyNoInteractions(outboxService);
    }
}