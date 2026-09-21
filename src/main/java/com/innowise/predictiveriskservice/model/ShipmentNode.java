package com.innowise.predictiveriskservice.model;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node({"Shipment", "Order"})
public class ShipmentNode extends AbstractSupplyChainNode {

    @Relationship(type = "ASSIGNED_TO", direction = Relationship.Direction.OUTGOING)
    private List<ContainerNode> containers;
}
