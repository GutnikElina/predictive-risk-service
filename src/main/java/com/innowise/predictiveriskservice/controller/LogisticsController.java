package com.innowise.predictiveriskservice.controller;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import com.innowise.predictiveriskservice.entity.ShipmentNode;
import com.innowise.predictiveriskservice.service.LogisticsGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsGraphService logisticsGraphService;

    @PostMapping("/shipments/{shipmentId}/containers/{containerId}")
    public Mono<ResponseEntity<ShipmentNode>> linkContainerToShipment(
            @PathVariable UUID containerId,
            @PathVariable UUID shipmentId) {

        return logisticsGraphService.assignShipmentToContainer(shipmentId, containerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/vessels/{vesselId}/containers/{containerId}")
    public Mono<ResponseEntity<ContainerNode>> linkContainerToVessel(
            @PathVariable UUID containerId,
            @PathVariable UUID vesselId) {

        return logisticsGraphService.loadContainerOnVessel(containerId, vesselId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
