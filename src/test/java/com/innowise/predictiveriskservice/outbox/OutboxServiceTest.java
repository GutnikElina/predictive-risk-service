package com.innowise.predictiveriskservice.outbox;

import com.innowise.predictiveriskservice.entity.KafkaEventTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxService outboxService;

    @BeforeEach
    void setUp() {
        outboxService = new OutboxService(outboxEventRepository);
    }

    @Test
    void save_buildsPendingEventWithZeroAttempts_andPersistsIt() {
        String payload = "{\"foo\":\"bar\"}";

        outboxService.save(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT, payload);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertThat(saved.getKafkaEventType()).isEqualTo(KafkaEventTypes.RISK_LEVEL_ESCALATED_EVENT);
        assertThat(saved.getPayload()).isEqualTo(payload);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getAttemptCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findEventsWithStatus_delegatesToRepository() {
        List<OutboxEvent> expected = List.of(
                OutboxEvent.builder().id(UUID.randomUUID()).status(OutboxStatus.PENDING).build()
        );
        when(outboxEventRepository.findOutboxEventByStatus(OutboxStatus.PENDING)).thenReturn(expected);

        List<OutboxEvent> result = outboxService.findEventsWithStatus(OutboxStatus.PENDING);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void setStatus_delegatesToRepositoryUpdate() {
        UUID id = UUID.randomUUID();

        outboxService.setStatus(id, OutboxStatus.PUBLISHED);

        verify(outboxEventRepository).updateEventStatus(id, OutboxStatus.PUBLISHED);
    }

    @Test
    void setErrorMessage_delegatesToRepositoryUpdate() {
        UUID id = UUID.randomUUID();

        outboxService.setErrorMessage(id, "boom");

        verify(outboxEventRepository).updateEventMessage(id, "boom");
    }
}