package com.innowise.predictiveriskservice.kafka.consumer;

import com.innowise.predictiveriskservice.kafka.avro.GeofencingEventAvro;
import com.innowise.predictiveriskservice.kafka.avro.GeofencingEventType;
import com.innowise.predictiveriskservice.service.LogisticsGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeofenceEventConsumer {

    private final LogisticsGraphService logisticsGraphService;

    @KafkaListener(topics = "logistics.events.geofencing.v1")
    public void consumeGeofencing(GeofencingEventAvro event) {
        if (event.getEventType() == GeofencingEventType.ENTERED) {
            UUID containerId = event.getDeviceId();
            UUID vesselId = event.getZoneId();

            logisticsGraphService.loadContainerOnVessel(containerId, vesselId)
                    .subscribe(
                            updated -> log.info("Linked Container {} to Vessel {}", containerId, vesselId),
                            error -> log.error("Failed to link Container {} to Vessel {}: {}",
                                    containerId, vesselId, error.getMessage())
                    );
        }
    }
}
