package com.innowise.predictiveriskservice.outbox;

import com.innowise.predictiveriskservice.model.enums.KafkaEventTypes;
import com.innowise.predictiveriskservice.model.enums.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;

    public void save(KafkaEventTypes kafkaEventTypes, String payload) {
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .kafkaEventType(kafkaEventTypes)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .attemptCount(0)
                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                .updatedAt(Instant.now().atOffset(ZoneOffset.UTC))
                .build();

        outboxEventRepository.save(outboxEvent);
    }

    public List<OutboxEvent> findEventsWithStatus(OutboxStatus status) {
        return outboxEventRepository.findOutboxEventByStatus(status);
    }

    public void setStatus(UUID eventId, OutboxStatus status) {
        outboxEventRepository.updateEventStatus(eventId, status);
    }

    public void setErrorMessage(UUID eventId, String message) {
        outboxEventRepository.updateEventMessage(eventId, message);
    }
}
