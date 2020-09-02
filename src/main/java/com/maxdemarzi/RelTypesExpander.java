package com.maxdemarzi;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterators;
import org.neo4j.internal.helpers.collection.NestingIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class RelTypesExpander implements PathExpander {
    ArrayList<RelationshipType> relationshipTypes;
    Long length;

    public RelTypesExpander(String types, Long length) {

        this.relationshipTypes = new ArrayList<>();
        for (String typeName : types.split(",")) {
            typeName = typeName.trim();
            this.relationshipTypes.add(RelationshipType.withName(typeName));
        }
        this.length = length;
    }


    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        // If we're past the length limit return an empty list
        if (path.length() > length) { return Collections.emptyList(); }

        // Get the last node along the current path
        final Node node = path.endNode();

        // Traverse all the relationship types in either direction
        return Iterators.asList(new NestingIterator<>(relationshipTypes.iterator()) {
            @Override
            protected Iterator<Relationship> createNestedIterator(RelationshipType relationshipType) {
                return node.getRelationships(relationshipType).iterator();
            }
        });
    }

    @Override
    public PathExpander reverse() {
        return this;
    }
}
