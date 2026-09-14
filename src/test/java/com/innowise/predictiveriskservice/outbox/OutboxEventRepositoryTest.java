package com.innowise.predictiveriskservice.outbox;

import com.innowise.predictiveriskservice.entity.KafkaEventTypes;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository repository;

    @Autowired
    private EntityManager entityManager;

    private OutboxEvent newEvent(OutboxStatus status) {
        return repository.save(OutboxEvent.builder()
                .kafkaEventType(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT)
                .payload("{}")
                .status(status)
                .attemptCount(0)
                .errorMessage(null)
                .createdAt(Instant.now().atOffset(ZoneOffset.UTC))
                .updatedAt(Instant.now().atOffset(ZoneOffset.UTC))
                .build());
    }

    @Test
    void findOutboxEventByStatus_returnsOnlyMatchingEvents() {
        OutboxEvent pending = newEvent(OutboxStatus.PENDING);
        newEvent(OutboxStatus.PUBLISHED);

        List<OutboxEvent> pendingEvents = repository.findOutboxEventByStatus(OutboxStatus.PENDING);

        assertThat(pendingEvents).extracting(OutboxEvent::getId).containsExactly(pending.getId());
    }

    @Test
    void updateEventStatus_persistsNewStatus() {
        OutboxEvent event = newEvent(OutboxStatus.PENDING);

        repository.updateEventStatus(event.getId(), OutboxStatus.PUBLISHED);
        repository.flush();
        entityManager.clear();

        OutboxEvent reloaded = repository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
    }

    @Test
    void updateEventMessage_persistsErrorMessage() {
        OutboxEvent event = newEvent(OutboxStatus.PUBLISH_FAILED);

        repository.updateEventMessage(event.getId(), "connection refused");
        repository.flush();
        entityManager.clear();

        OutboxEvent reloaded = repository.findById(event.getId()).orElseThrow();
        assertThat(reloaded.getErrorMessage()).isEqualTo("connection refused");
    }
}