package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Vessel")
public class VesselNode extends AbstractSupplyChainNode {

    @Relationship(type = "BOUND_TO", direction = Relationship.Direction.OUTGOING)
    private PortNode port;
}
