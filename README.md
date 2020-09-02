# One Direction
Stored Procedure in Neo4j 4.1.x

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/one_direction-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/one_direction-1.0-SNAPSHOT.jar neo4j-enterprise-4.1.X/plugins/.
    
In the "neo4j.conf" file inside the Neo4j/conf folder add this line:

    dbms.security.procedures.unrestricted=com.maxdemarzi.*

You will need to create a single property index on any property you intend to use in a Range Query.
You should also create an index on any property you will use for a Contains query.

Stored Procedures:

    // YIELD path 
    CALL com.maxdemarzi.one.direction(start, end, types, length); 
    
    // Example using sample data
    MATCH (u1:User { name: 'max' } ), (u4:User { name: 'luke' } ) WITH u1, u4 
    CALL com.maxdemarzi.one.direction(u1, u4, 'KNOWS, LIKES', 5) 
    YIELD path 
    RETURN count(path) AS count;
    
Sample data:

    CREATE (u1:User { name: 'max' } )
    CREATE (u2:User { name: 'alex' } )
    CREATE (u3:User { name: 'michael' } )
    CREATE (u4:User { name: 'luke' } )
    CREATE (u5:User { name: 'mark' } )
    CREATE (u6:User { name: 'david' } )
    CREATE (u7:User { name: 'yufei' } )
    // 1 > 2 > 3 > 4 (Valid)
    CREATE (u1)-[:KNOWS]->(u2)
    CREATE (u2)-[:KNOWS]->(u3)
    CREATE (u3)-[:KNOWS]->(u4)
    // 1 > 2 < 5 < 4 (Valid)
    CREATE (u2)<-[:LIKES]-(u5)
    CREATE (u5)<-[:LIKES]-(u4)
    // 1 > 2 < 6 > 4 (Invalid)
    CREATE (u2)<-[:KNOWS]-(u6)
    CREATE (u6)-[:KNOWS]->(u4)
    // 1 > 2 < 6 < 8 (Invalid due to rel type)
    CREATE (u5)<-[:HATES]-(u4)       
    
    
    // One Filter
    CALL com.maxdemarzi.boolean.filter("Order", {not:false, and:[
        {property: "color", values: ["Blue"], not: false}
    ]}, 10) 
