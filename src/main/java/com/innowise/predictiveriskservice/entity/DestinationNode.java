package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Node("Destination")
public class DestinationNode {

    @Id
    @GeneratedValue
    private UUID id;
}
