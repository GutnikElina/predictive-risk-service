package com.innowise.predictiveriskservice.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findOutboxEventByStatus(OutboxStatus status);

    @Modifying
    @Query("update OutboxEvent e set e.status = :status where e.id = :eventId")
    void updateEventStatus(UUID eventId, OutboxStatus status);

    @Modifying
    @Query("update OutboxEvent e set e.errorMessage = :message where e.id = :eventId")
    void updateEventMessage(UUID eventId, String message);
}
