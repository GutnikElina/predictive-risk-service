package com.innowise.predictiveriskservice.kafka.consumer;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import com.innowise.predictiveriskservice.kafka.avro.GeofencingEventAvro;
import com.innowise.predictiveriskservice.kafka.avro.GeofencingEventType;
import com.innowise.predictiveriskservice.service.LogisticsGraphService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeofenceEventConsumerTest {

    @Mock
    private LogisticsGraphService logisticsGraphService;

    @InjectMocks
    private GeofenceEventConsumer consumer;

    @Test
    void consumeGeofencing_Entered() {
        UUID containerId = UUID.randomUUID();
        UUID vesselId = UUID.randomUUID();

        GeofencingEventAvro event = GeofencingEventAvro.newBuilder()
                .setEventId(UUID.randomUUID())
                .setDeviceId(containerId)
                .setZoneId(vesselId)
                .setEventType(GeofencingEventType.ENTERED)
                .setTimestamp(System.currentTimeMillis())
                .setLocation("home")
                .build();

        when(logisticsGraphService.loadContainerOnVessel(containerId, vesselId))
                .thenReturn(Mono.just(new ContainerNode()));

        consumer.consumeGeofencing(event);

        verify(logisticsGraphService, times(1)).loadContainerOnVessel(containerId, vesselId);
    }

    @Test
    void consumeGeofencing_Ignored() {
        GeofencingEventAvro event = GeofencingEventAvro.newBuilder()
                .setEventId(UUID.randomUUID())
                .setDeviceId(UUID.randomUUID())
                .setZoneId(UUID.randomUUID())
                .setEventType(GeofencingEventType.EXITED)
                .setTimestamp(System.currentTimeMillis())
                .setLocation("home")
                .build();

        consumer.consumeGeofencing(event);

        verifyNoInteractions(logisticsGraphService);
    }
}
