package com.maxdemarzi;

import com.maxdemarzi.results.PathResult;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Procedures {


    @Context
    public Transaction tx;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final OneDirectionChangeEvaluator ONE_DIRECTION_CHANGE_EVALUATOR = new OneDirectionChangeEvaluator();

    @Procedure(name = "com.maxdemarzi.one.direction", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.one.direction(start, end, types, length)")
    public Stream<PathResult> OneDirection(@Name(value = "start") Node start,
                                           @Name(value = "end") Node end,
                                           @Name(value = "types") String types,
                                           @Name(value = "length") Long length) {

        // Create a description of our traversal that expands the
        TraversalDescription td = tx.traversalDescription()
                .breadthFirst()
                .expand(new RelTypesExpander(types, length))
                .uniqueness(Uniqueness.NODE_PATH);

        BidirectionalTraversalDescription bidirtd = tx.bidirectionalTraversalDescription()
                .mirroredSides(td)
                .collisionEvaluator(ONE_DIRECTION_CHANGE_EVALUATOR);

        return StreamSupport.stream(bidirtd.traverse(start, end).spliterator(), false).map(PathResult::new);

    }

}
