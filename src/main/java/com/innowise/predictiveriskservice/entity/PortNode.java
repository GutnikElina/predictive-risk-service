package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Port")
public class PortNode extends SupplyChainNode {

    @Relationship(type = "DISCHARGED_AT", direction = Relationship.Direction.OUTGOING)
    private TruckNode truck;
}
