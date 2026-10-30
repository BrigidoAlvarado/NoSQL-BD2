package com.nosql.apiadmisionesusac.graph;

import org.neo4j.driver.Driver;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Aplica filtros y coordina la consulta de la muestra visual. */
@Service
public class GraphExplorerService {
    private final Driver driver;
    private final GraphQueryService queries;
    private final GraphResponseMapper mapper = new GraphResponseMapper();

    public GraphExplorerService(Driver driver, GraphQueryService queries) {
        this.driver = driver;
        this.queries = queries;
    }

    public GraphResponse explore(
            Integer anio,
            String carrera,
            String resultado,
            String departamento,
            int requestedLimit) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("anio", anio);
        parameters.put("carrera", blankToNull(carrera));
        parameters.put("resultado", blankToNull(resultado));
        parameters.put("departamento", blankToNull(departamento));
        parameters.put("limite", Math.clamp(requestedLimit, 1, 150));
        String cypher = CypherResources.read("queries/neo4j/explorar_grafo.cypher");
        try (var session = driver.session()) {
            return session.executeRead(tx -> mapper.map(tx.run(cypher, parameters).list()));
        }
    }

    public Map<String, java.util.List<String>> options() {
        try (var session = driver.session()) {
            return session.executeRead(tx -> {
                var careers = tx.run("MATCH (c:Carrera) RETURN DISTINCT c.clave AS valor ORDER BY valor")
                        .list(row -> row.get("valor").asString());
                var departments = tx.run("MATCH (d:Departamento) RETURN DISTINCT d.clave AS valor ORDER BY valor")
                        .list(row -> row.get("valor").asString());
                return Map.of("carreras", careers, "departamentos", departments);
            });
        }
    }

    public GraphQueryResponse query(String id, java.util.Map<String, String> filters) {
        return queries.execute(id, new java.util.HashMap<>(filters));
    }

    public GraphQueryResponse query(String id) {
        return queries.execute(id);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
