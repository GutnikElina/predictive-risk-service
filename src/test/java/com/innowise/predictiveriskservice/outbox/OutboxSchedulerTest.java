package com.innowise.predictiveriskservice.outbox;

import com.innowise.predictiveriskservice.model.enums.KafkaEventTypes;
import com.innowise.predictiveriskservice.kafka.producer.KafkaPublisher;
import com.innowise.predictiveriskservice.model.enums.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxSchedulerTest {

    @Mock
    private KafkaPublisher kafkaPublisher;
    @Mock
    private OutboxService outboxService;

    private OutboxScheduler outboxScheduler;

    @BeforeEach
    void setUp() {
        outboxScheduler = new OutboxScheduler(kafkaPublisher, outboxService);
    }

    private OutboxEvent pendingEvent(int attemptCount) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .kafkaEventType(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT)
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .attemptCount(attemptCount)
                .errorMessage("")
                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                .updatedAt(Instant.now().atOffset(ZoneOffset.UTC))
                .build();
    }

    @Test
    void publishPendingEvents_success_marksEventPublished() {
        OutboxEvent event = pendingEvent(0);
        when(outboxService.findEventsWithStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));
        when(kafkaPublisher.sendEvent(event.getKafkaEventType().kafkaTopic, event.getPayload()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        outboxScheduler.publishPendingEvents();

        verify(outboxService).setStatus(event.getId(), OutboxStatus.PUBLISHED);
        verify(outboxService, never()).setErrorMessage(any(), any());
    }

    @Test
    void retryFailedEvents_fetchesPublishFailedEvents_andPublishesThem() {
        OutboxEvent event = pendingEvent(3);
        event.setStatus(OutboxStatus.PUBLISH_FAILED);
        when(outboxService.findEventsWithStatus(OutboxStatus.PUBLISH_FAILED)).thenReturn(List.of(event));
        when(kafkaPublisher.sendEvent(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        outboxScheduler.retryFailedEvents();

        verify(outboxService).setStatus(event.getId(), OutboxStatus.PUBLISHED);
    }

    @Test
    void publishPendingEvents_publishFailure_recordsErrorMessage_andDoesNotQuitYet() {
        OutboxEvent event = pendingEvent(0);
        when(outboxService.findEventsWithStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));
        when(kafkaPublisher.sendEvent(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        outboxScheduler.publishPendingEvents();

        verify(outboxService).setErrorMessage(eq(event.getId()), argThat(msg -> msg.contains("kafka down")));
        verify(outboxService, never()).setStatus(event.getId(), OutboxStatus.QUIT_PUBLISHING);
    }

    @Test
    void publishPendingEvents_failureAtMaxAttempts_marksQuitPublishing() {
        OutboxEvent event = pendingEvent(9);
        when(outboxService.findEventsWithStatus(OutboxStatus.PENDING)).thenReturn(List.of(event));
        when(kafkaPublisher.sendEvent(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        outboxScheduler.publishPendingEvents();

        verify(outboxService).setStatus(event.getId(), OutboxStatus.QUIT_PUBLISHING);
    }

    @Test
    void publishPendingEvents_noEventsFound_doesNothing() {
        when(outboxService.findEventsWithStatus(OutboxStatus.PENDING)).thenReturn(List.of());

        outboxScheduler.publishPendingEvents();

        verify(kafkaPublisher, never()).sendEvent(any(), any());
    }
}