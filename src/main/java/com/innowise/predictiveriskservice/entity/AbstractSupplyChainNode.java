package com.innowise.predictiveriskservice.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.UUID;

@Getter
@Setter
@Node("SupplyChainNode")
public abstract class AbstractSupplyChainNode {

    @Id
    @GeneratedValue
    private UUID id;
}
