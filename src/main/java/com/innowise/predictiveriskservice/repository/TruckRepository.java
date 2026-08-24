package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.TruckNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TruckRepository extends ReactiveNeo4jRepository<TruckNode, UUID> {

    @Query("MATCH (t:Truck {id: $truckId}), (d:Destination {id: $destinationId}) " +
            "MERGE (t)-[r:DESTINED_FOR]->(d) " +
            "RETURN t")
    Mono<TruckNode> linkDestinationToTruck(UUID truckId, UUID destinationId);
}
