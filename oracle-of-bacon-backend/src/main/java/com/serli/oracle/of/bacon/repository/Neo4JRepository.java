package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.v1.*;

import java.util.List;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        driver = GraphDatabase.driver("http://localhost:7474", AuthTokens.basic("neo4j", "root"));
    }

    public List<?> getConnectionsToKevinBacon(String actorName) {
        Session session = driver.session();

        StatementResult result = session.run("MATCH (cs { name: 'Bacon, Kevin (I)' }),(ms { name: '" + actorName + "' }), " +
                "p = shortestPath((cs)-[*]-(ms)) " +
                "WITH p WHERE length(p) > 1 RETURN p");

        return result.list();
    }

    private static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }
    }
}
