package com.innowise.predictiveriskservice.service;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import com.innowise.predictiveriskservice.entity.ShipmentNode;
import com.innowise.predictiveriskservice.exception.EntityNotFoundException;
import com.innowise.predictiveriskservice.exception.RelationshipCreationException;
import com.innowise.predictiveriskservice.repository.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogisticsGraphServiceTest {

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @InjectMocks
    private LogisticsGraphService logisticsGraphService;

    private UUID shipmentId;
    private UUID containerId;
    private UUID vesselId;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        containerId = UUID.randomUUID();
        vesselId = UUID.randomUUID();
    }

    @Test
    void assignShipmentToContainer_Success() {
        ShipmentNode mockShipment = new ShipmentNode();
        mockShipment.setId(shipmentId);

        when(supplyChainRepository.assignShipmentToContainer(containerId, shipmentId))
                .thenReturn(Mono.just(mockShipment));

        StepVerifier.create(logisticsGraphService.assignShipmentToContainer(shipmentId, containerId))
                .expectNext(mockShipment)
                .verifyComplete();

        verify(supplyChainRepository).assignShipmentToContainer(containerId, shipmentId);
    }

    @Test
    void assignShipmentToContainer_NotFound() {
        when(supplyChainRepository.assignShipmentToContainer(containerId, shipmentId))
                .thenReturn(Mono.empty());

        StepVerifier.create(logisticsGraphService.assignShipmentToContainer(shipmentId, containerId))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    void assignShipmentToContainer_DataIntegrityException() {
        when(supplyChainRepository.assignShipmentToContainer(containerId, shipmentId))
                .thenReturn(Mono.error(new DataIntegrityViolationException("Constraint error")));

        StepVerifier.create(logisticsGraphService.assignShipmentToContainer(shipmentId, containerId))
                .expectError(RelationshipCreationException.class)
                .verify();
    }

    @Test
    void loadContainerOnVessel_Success() {
        ContainerNode mockContainer = new ContainerNode();
        mockContainer.setId(containerId);

        when(supplyChainRepository.loadContainerOnVessel(containerId, vesselId))
                .thenReturn(Mono.just(mockContainer));

        StepVerifier.create(logisticsGraphService.loadContainerOnVessel(containerId, vesselId))
                .expectNext(mockContainer)
                .verifyComplete();
    }
}
