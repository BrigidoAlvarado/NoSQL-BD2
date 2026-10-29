package com.nosql.apiadmisionesusac.graph;

import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lee el recurso Cypher y devuelve las columnas que la consulta realmente produce. */
@Service
public class GraphQueryService {
    private final Driver driver;

    public GraphQueryService(Driver driver) {
        this.driver = driver;
    }

    public GraphQueryResponse execute(String id) {
        return execute(id, Map.of());
    }

    public GraphQueryResponse execute(String id, Map<String, Object> parameters) {
        GraphQueryCatalog.Query query = GraphQueryCatalog.find(id);
        String cypher = CypherResources.read(query.resource());
        try (var session = driver.session()) {
            return session.executeRead(
                    tx -> {
                        var result = tx.run(cypher, parameters);
                        List<String> columns = result.keys();
                        List<Map<String, Object>> rows =
                                result.list(
                                        record -> {
                                            Map<String, Object> row = new LinkedHashMap<>();
                                            for (String column : columns)
                                                row.put(column, record.get(column).asObject());
                                            return row;
                                        });
                        return new GraphQueryResponse(
                                id, query.title(), query.description(), columns, rows);
                    });
        }
    }
}
