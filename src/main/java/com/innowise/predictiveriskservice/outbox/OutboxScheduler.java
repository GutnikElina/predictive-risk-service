package com.innowise.predictiveriskservice.outbox;

import com.innowise.predictiveriskservice.kafka.producer.KafkaPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxScheduler {
    private final KafkaPublisher kafkaPublisher;
    private final OutboxService outboxService;

    private static final int MAX_ATTEMPTS = 10;
    private static final long PUBLISHING_LOOP_MILLISECONDS = 1000L;
    private static final long RETRYING_LOOP_MILLISECONDS = 15000L;

    @Scheduled(fixedDelay = PUBLISHING_LOOP_MILLISECONDS)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxService.findEventsWithStatus(OutboxStatus.PENDING);
        publishEvents(events);
    }

    @Scheduled(fixedDelay = RETRYING_LOOP_MILLISECONDS)
    public void retryFailedEvents() {
        List<OutboxEvent> events = outboxService.findEventsWithStatus(OutboxStatus.PUBLISH_FAILED);
        publishEvents(events);
    }

    private void publishEvents(List<OutboxEvent> events) {
        for (var event : events) {
            kafkaPublisher.sendEvent(event.getKafkaEventType().kafkaTopic, event.getPayload())
                    .thenAccept(result ->
                            outboxService.setStatus(event.getId(), OutboxStatus.PUBLISHED)
                    )
                    .exceptionally(ex -> {
                        log.error("Failed to publish event {}", event.getId(), ex);

                        String errorMessage = event.getErrorMessage() + '\n' + ex.getMessage();
                        outboxService.setErrorMessage(event.getId(), errorMessage);

                        Integer currentAttempts = event.getAttemptCount();
                        currentAttempts += 1;

                        if (currentAttempts < MAX_ATTEMPTS) {
                            event.setAttemptCount(currentAttempts);
                        } else {
                            outboxService.setStatus(event.getId(), OutboxStatus.QUIT_PUBLISHING);
                        }

                        return null;
                    })
            ;
        }
    }
}
