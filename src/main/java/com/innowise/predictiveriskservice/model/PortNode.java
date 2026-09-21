package com.innowise.predictiveriskservice.model;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Port")
public class PortNode extends AbstractSupplyChainNode {

    @Relationship(type = "DISCHARGED_AT", direction = Relationship.Direction.OUTGOING)
    private TruckNode truck;
}
