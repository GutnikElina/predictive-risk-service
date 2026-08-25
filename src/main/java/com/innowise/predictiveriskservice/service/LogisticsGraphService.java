package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import com.innowise.predictiveriskservice.entity.ShipmentNode;
import com.innowise.predictiveriskservice.exception.EntityNotFoundException;
import com.innowise.predictiveriskservice.exception.RelationshipCreationException;
import com.innowise.predictiveriskservice.repository.ContainerRepository;
import com.innowise.predictiveriskservice.repository.ShipmentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    public Mono<ShipmentNode> assignShipmentToContainer(UUID shipmentId, UUID containerId) {
        return shipmentOrderRepository.assignShipmentToContainer(containerId, shipmentId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Shipment or Container", shipmentId)
                ))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new RelationshipCreationException(
                                String.format(
                                        "Failed to link shipment %s to container %s: Relationship conflict.",
                                        shipmentId,
                                        containerId
                                )
                        ));
    }

    public Mono<ContainerNode> loadContainerOnVessel(UUID containerId, UUID vesselId) {
        return containerRepository.loadContainerOnVessel(containerId, vesselId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Vessel or Container", vesselId)
                ))
                .onErrorMap(DataIntegrityViolationException.class,
                        ex -> new RelationshipCreationException(
                                String.format(
                                        "Failed to link container %s to vessel %s: Relationship conflict.",
                                        containerId,
                                        vesselId
                                )
                        ));
    }
}
