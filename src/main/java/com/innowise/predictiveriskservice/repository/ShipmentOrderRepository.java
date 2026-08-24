package com.innowise.predictiveriskservice.repository;

import com.innowise.predictiveriskservice.entity.ShipmentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;

import java.util.UUID;

public interface ShipmentRepository extends ReactiveNeo4jRepository<ShipmentNode, UUID> {
}
