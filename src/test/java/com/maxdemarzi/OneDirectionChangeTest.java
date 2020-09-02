package com.maxdemarzi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import static org.junit.Assert.assertEquals;

public class OneDirectionChangeTest {
    private static Neo4j neo4j;

    @BeforeAll
    static void initialize() {
        neo4j = Neo4jBuilders.newInProcessBuilder()
                // disabling http server to speed up start
                .withDisabledServer()
                .withProcedure(Procedures.class)
                .withFixture(MODEL_STATEMENT)
                .build();
    }

    @AfterAll
    static void stopNeo4j() {
        neo4j.close();
    }

    @Test
    void shouldFindValidPaths() {
        // In a try-block, to make sure we close the driver after the test
        try(Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.builder().withoutEncryption().build())) {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            Result result = session.run("MATCH (u1:User { name: 'max' } ), (u4:User { name: 'luke' } ) WITH u1, u4 " +
                    "CALL com.maxdemarzi.one.direction(u1, u4, 'KNOWS, LIKES', 5) YIELD path RETURN count(path) AS count");
            Record record = result.single();
            assertEquals(2L, record.get("count").asLong());
        }
    }


    private static final String MODEL_STATEMENT =
            "CREATE (u1:User { name: 'max' } ) " +
            "CREATE (u2:User { name: 'alex' } ) " +
            "CREATE (u3:User { name: 'michael' } ) " +
            "CREATE (u4:User { name: 'luke' } ) " +
            "CREATE (u5:User { name: 'mark' } ) " +
            "CREATE (u6:User { name: 'david' } ) " +
            "CREATE (u7:User { name: 'yufei' } ) " +
            // 1 > 2 > 3 > 4 (Valid)
            "CREATE (u1)-[:KNOWS]->(u2) " +
            "CREATE (u2)-[:KNOWS]->(u3) " +
            "CREATE (u3)-[:KNOWS]->(u4) " +
            // 1 > 2 < 5 < 4 (Valid)
            "CREATE (u2)<-[:LIKES]-(u5) " +
            "CREATE (u5)<-[:LIKES]-(u4) " +
            // 1 > 2 < 6 > 4 (Invalid)
            "CREATE (u2)<-[:KNOWS]-(u6) " +
            "CREATE (u6)-[:KNOWS]->(u4) " +
            // 1 > 2 < 6 < 8 (Invalid due to rel type)
            "CREATE (u5)<-[:HATES]-(u4) " ;

}
