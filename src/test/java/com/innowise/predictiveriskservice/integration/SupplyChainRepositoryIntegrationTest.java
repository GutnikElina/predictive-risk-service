package com.innowise.predictiveriskservice.integration;

import com.innowise.predictiveriskservice.entity.VesselNode;
import com.innowise.predictiveriskservice.repository.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    void loadContainerOnVessel_CreatesRelationship() {
        StepVerifier.create(supplyChainRepository.loadContainerOnVessel(containerId, vesselId))
                .expectNextMatches(container -> container.getId().equals(containerId))
                .verifyComplete();
    }

    @Test
    void findAllAffectedVesselsAndContainers_ReturnsEmpty_WhenNoAffectedNodes() {
        UUID emptyPortId = UUID.randomUUID();

        neo4jClient.query("CREATE (p:Port {id: $portId})")
                .bind(emptyPortId.toString()).to("portId")
                .run()
                .block();

        StepVerifier.create(supplyChainRepository.findAllAffectedVesselsAndContainers(emptyPortId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllAffectedVesselsAndContainers_ReturnsEmpty_WhenPortDoesNotExist() {
        UUID nonExistentPortId = UUID.randomUUID();

        StepVerifier.create(supplyChainRepository.findAllAffectedVesselsAndContainers(nonExistentPortId))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findAllAffectedVesselsAndContainers_ReturnsOneHopVessel() {
        UUID portId = UUID.randomUUID();
        UUID boundVesselId = UUID.randomUUID();

        neo4jClient.query("CREATE (p:Port {id: $portId}), (v:Vessel {id: $vesselId}), (v)-[:BOUND_TO]->(p)")
                .bind(portId.toString()).to("portId")
                .bind(boundVesselId.toString()).to("vesselId")
                .run()
                .block();

        StepVerifier.create(supplyChainRepository.findAllAffectedVesselsAndContainers(portId))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(m -> true)
                .consumeRecordedWith(results -> {
                    System.out.println("Results: " + results);
                    results.forEach(n -> System.out.println("  -> " + n.getClass().getSimpleName()));
                    assertEquals(1, results.size());
                    assertInstanceOf(VesselNode.class, results.toArray()[0]);
                })
                .verifyComplete();
    }
}