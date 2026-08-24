package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.VesselNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VesselRepository extends ReactiveNeo4jRepository<VesselNode, UUID> {

    @Query("MATCH (v:Vessel {id: $vesselId}), (p:Port {id: $portId}) " +
            "MERGE (v)-[r:BOUND_TO]->(p) " +
            "RETURN v")
    Mono<VesselNode> linkPortToVessel(UUID vesselId, UUID portId);
}
