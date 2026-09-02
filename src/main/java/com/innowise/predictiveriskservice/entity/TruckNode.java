package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Truck")
public class TruckNode extends AbstractSupplyChainNode {

    @Relationship(type = "DESTINED_FOR", direction = Relationship.Direction.OUTGOING)
    private DestinationNode destination;
}
