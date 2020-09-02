package com.maxdemarzi;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

public class OneDirectionChangeEvaluator implements Evaluator {
    @Override
    public Evaluation evaluate(Path path) {
        long nodeId = path.startNode().getId();

        if(path.length() < 3) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }

        Direction direction = Direction.BOTH;
        int changes = -1;

        for(Relationship rel :path.relationships()) {
            if(rel.getStartNodeId() == nodeId) {
                if(direction != Direction.OUTGOING) {
                    changes++;
                }
                direction = Direction.OUTGOING;
            } else {
                if(direction != Direction.INCOMING) {
                    changes++;
                }
                direction = Direction.INCOMING;
            }
            nodeId = rel.getOtherNodeId(nodeId);
        }
        if (changes < 2) {
            return Evaluation.INCLUDE_AND_PRUNE;
        } else {
            return Evaluation.EXCLUDE_AND_PRUNE;
        }


    }
}
