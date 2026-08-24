package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.UUID;

@Node("Container")
public class ContainerNode {

    @Id
    @GeneratedValue
    private UUID id;

    @Relationship(type = "LOADED_ON", direction = Relationship.Direction.OUTGOING)
    private VesselNode vessel;
}
