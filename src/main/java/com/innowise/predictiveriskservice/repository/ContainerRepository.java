package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.ContainerNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ContainerRepository extends ReactiveNeo4jRepository<ContainerNode, UUID> {

    @Query("MATCH (c:Container {id: $containerId}), (v:Vessel {id: $vesselId}) " +
            "MERGE (c)-[r:LOADED_ON]->(v) " +
            "RETURN c")
    Mono<ContainerNode> loadContainerOnVessel(UUID containerId, UUID vesselId);
}
