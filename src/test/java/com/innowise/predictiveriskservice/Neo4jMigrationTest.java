package com.innowise.predictiveriskservice;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Neo4jMigrationTest {

    private static Neo4jContainer neo4jContainer;

    @BeforeAll
    static void initializeNeo4j() {
        neo4jContainer = new Neo4jContainer()
                .withAdminPassword("testPassword");
        neo4jContainer.start();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4jContainer.close();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4jContainer::getAdminPassword);
    }

    @Test
    void verifyMigrationsAreAppliedOnStartup(@Autowired Neo4jClient client) {
        Optional<Long> appliedMigrationsCount = client.query(
                "MATCH (m:__Neo4jMigration) RETURN count(m) AS count"
        ).fetchAs(Long.class).one();

        assertThat(appliedMigrationsCount).isPresent();
        assertThat(appliedMigrationsCount.get()).isGreaterThan(0L);

        Long constraintsCount = client.query(
                "SHOW CONSTRAINTS YIELD name RETURN count(name) AS count"
        ).fetchAs(Long.class).one().orElse(0L);

        assertThat(constraintsCount).isGreaterThan(0L);
    }
}