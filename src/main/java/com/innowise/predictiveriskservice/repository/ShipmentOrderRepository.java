package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.ShipmentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.UUID;

public interface ShipmentOrderRepository extends ReactiveNeo4jRepository<ShipmentNode, UUID> {

    @Query("MATCH (s:Shipment {id: $shipmentId}), (c:Container {id: $containerId}) " +
            "MERGE (s)-[r:ASSIGNED_TO]->(c) " +
            "RETURN s")
    ShipmentNode linkContainerToShipment(UUID shipmentId, UUID containerId);
}
