package com.innowise.predictiveriskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;

@SpringBootApplication
public class PredictiveRiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PredictiveRiskServiceApplication.class, args);
    }

}
