package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import com.innowise.predictiveriskservice.entity.ShipmentNode;
import com.innowise.predictiveriskservice.repository.ContainerRepository;
import com.innowise.predictiveriskservice.repository.ShipmentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LogisticsGraphService {

    private final ContainerRepository containerRepository;
    private final ShipmentOrderRepository shipmentOrderRepository;

    public Mono<ShipmentNode> linkContainerToShipment(String shipmentId, String containerId) {
        return shipmentOrderRepository.linkContainerToShipment(
                UUID.fromString(shipmentId),
                UUID.fromString(containerId)
        );
    }

    public Mono<ContainerNode> linkVesselToContainer(String containerId, String vesselId) {
        return containerRepository.linkVesselToContainer(
                UUID.fromString(containerId),
                UUID.fromString(vesselId)
        );
    }
}
