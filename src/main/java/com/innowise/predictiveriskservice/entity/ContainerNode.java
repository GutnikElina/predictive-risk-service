package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Container")
public class ContainerNode extends SupplyChainNode {

    @Relationship(type = "LOADED_ON", direction = Relationship.Direction.OUTGOING)
    private VesselNode vessel;
}
