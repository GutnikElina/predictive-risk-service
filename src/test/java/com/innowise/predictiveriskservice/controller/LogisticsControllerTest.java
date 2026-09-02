package com.innowise.predictiveriskservice.controller;

import com.innowise.predictiveriskservice.entity.ShipmentNode;
import com.innowise.predictiveriskservice.exception.EntityNotFoundException;
import com.innowise.predictiveriskservice.repository.SupplyChainRepository;
import com.innowise.predictiveriskservice.service.LogisticsGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = LogisticsController.class)
@ActiveProfiles("test")
class LogisticsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LogisticsGraphService logisticsGraphService;

    @MockitoBean
    private SupplyChainRepository supplyChainRepository;

    @Test
    void linkContainerToShipment_Success() {
        UUID shipmentId = UUID.randomUUID();
        UUID containerId = UUID.randomUUID();
        ShipmentNode shipment = new ShipmentNode();
        shipment.setId(shipmentId);

        when(logisticsGraphService.assignShipmentToContainer(shipmentId, containerId))
                .thenReturn(Mono.just(shipment));

        webTestClient.post()
                .uri("/api/v1/logistics/shipments/{shipmentId}/containers/{containerId}", shipmentId, containerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(shipmentId.toString());
    }

    @Test
    void linkContainerToShipment_NotFound() {
        UUID shipmentId = UUID.randomUUID();
        UUID containerId = UUID.randomUUID();

        when(logisticsGraphService.assignShipmentToContainer(shipmentId, containerId))
                .thenReturn(Mono.error(new EntityNotFoundException("Shipment or Container", shipmentId)));

        webTestClient.post()
                .uri("/api/v1/logistics/shipments/{shipmentId}/containers/{containerId}", shipmentId, containerId)
                .exchange()
                .expectStatus().isNotFound();
    }
}
