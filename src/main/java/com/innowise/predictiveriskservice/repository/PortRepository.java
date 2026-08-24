package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.PortNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.UUID;

public interface PortRepository extends ReactiveNeo4jRepository<PortNode, UUID> {

    @Query("MATCH (p:Port {id: $portId}), (t:Truck {id: $truckId}) " +
            "MERGE (p)-[r:DISCHARGED_AT]->(t) " +
            "RETURN p")
    PortNode linkTruckToPort(UUID portId, UUID truckId);
}
