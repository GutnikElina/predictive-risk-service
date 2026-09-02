package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.*;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SupplyChainRepository extends ReactiveNeo4jRepository<AbstractSupplyChainNode, UUID> {

    @Query("MATCH (s:Shipment {id: $shipmentId}), (c:Container {id: $containerId}) " +
            "MERGE (s)-[r:ASSIGNED_TO]->(c) " +
            "RETURN s")
    Mono<ShipmentNode> assignShipmentToContainer(UUID containerId, UUID shipmentId);

    @Query("MATCH (c:Container {id: $containerId}), (v:Vessel {id: $vesselId}) " +
            "MERGE (c)-[r:LOADED_ON]->(v) " +
            "RETURN c")
    Mono<ContainerNode> loadContainerOnVessel(UUID containerId, UUID vesselId);

    @Query("MATCH (p:Port {id: $portId})<-[:BOUND_TO|LOADED_ON*1..2]-(affected) " +
            "RETURN affected")
    Flux<AbstractSupplyChainNode> findAllAffectedVesselsAndContainers(UUID portId);
}
