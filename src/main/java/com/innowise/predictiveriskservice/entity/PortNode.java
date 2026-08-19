package com.innowise.predictiveriskservice.entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node("Port")
public class PortNode {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;
}
