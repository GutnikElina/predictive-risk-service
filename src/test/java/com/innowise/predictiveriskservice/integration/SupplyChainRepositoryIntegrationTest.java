package com.innowise.predictiveriskservice.integration;

import com.innowise.predictiveriskservice.repository.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class SupplyChainRepositoryIntegrationTest {

    @Container
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:5-community")
            .withoutAuthentication();

    @DynamicPropertySource
    static void setNeo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
    }

    @Autowired
    private SupplyChainRepository supplyChainRepository;

    @Autowired
    private ReactiveNeo4jClient neo4jClient;

    private UUID shipmentId;
    private UUID containerId;
    private UUID vesselId;

    @BeforeEach
    void setUp() {
        shipmentId = UUID.randomUUID();
        containerId = UUID.randomUUID();
        vesselId = UUID.randomUUID();

        neo4jClient.query("MATCH (n) DETACH DELETE n").run().block();

        neo4jClient.query("CREATE (s:Shipment {id: $sId}), (c:Container {id: $cId}), (v:Vessel {id: $vId})")
                .bind(shipmentId.toString()).to("sId")
                .bind(containerId.toString()).to("cId")
                .bind(vesselId.toString()).to("vId")
                .run()
                .block();
    }

    @Test
    @DisplayName("assignShipmentToContainer - Successfully links nodes in Neo4j")
    void assignShipmentToContainer_CreatesRelationship() {
        StepVerifier.create(supplyChainRepository.assignShipmentToContainer(containerId, shipmentId))
                .expectNextMatches(shipment -> shipment.getId().equals(shipmentId))
                .verifyComplete();

        String verifyCypher = "MATCH (s:Shipment {id: $sId})-[r:ASSIGNED_TO]->(c:Container {id: $cId}) RETURN count(r)";

        StepVerifier.create(neo4jClient.query(verifyCypher)
                        .bind(shipmentId.toString()).to("sId")
                        .bind(containerId.toString()).to("cId")
                        .fetchAs(Long.class)
                        .one())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    @DisplayName("loadContainerOnVessel - Successfully links Container and Vessel")
    void loadContainerOnVessel_CreatesRelationship() {
        StepVerifier.create(supplyChainRepository.loadContainerOnVessel(containerId, vesselId))
                .expectNextMatches(container -> container.getId().equals(containerId))
                .verifyComplete();
    }
}