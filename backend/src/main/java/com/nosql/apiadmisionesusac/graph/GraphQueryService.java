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
        if (!parameters.isEmpty()) {
            var categories = Map.of(
                "G01", "carrera", "G02", "materia,resultado", "G03", "departamento,carrera",
                "G04", "carreraOrigen,carreraDestino", "G05", "anioIngreso,carrera,materia,resultado",
                "G06", "tipoInstitucion,materia,resultado", "G07", "anioIngreso,materia",
                "G08", "departamento,municipio,carrera", "G09", "sexo,carrera,materia,resultado", "G10", "carrera");
            var allowed = java.util.Set.of(categories.get(id).split(","));
            var conditions = new java.util.ArrayList<String>();
            var bound = new java.util.HashMap<String, Object>();
            for (var entry : parameters.entrySet()) {
                if (!allowed.contains(entry.getKey()) || !(entry.getValue() instanceof String value) || value.length() > 200) {
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Filtro no permitido.");
                }
                if (value.isEmpty()) continue;
                Object literal = value;
                if (entry.getKey().equals("anioIngreso")) {
                    try { literal = Integer.valueOf(value); }
                    catch (NumberFormatException ex) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Año inválido."); }
                }
                bound.put(entry.getKey(), literal);
                conditions.add(entry.getKey() + " = $" + entry.getKey());
            }
            parameters = bound;
            if (!conditions.isEmpty()) cypher = "CALL {\n" + cypher + "\n} WITH * WHERE " + String.join(" AND ", conditions) + " RETURN *";
        }
        final String statement = cypher;
        final Map<String, Object> bindings = parameters;
        try (var session = driver.session()) {
            return session.executeRead(
                    tx -> {
                        var result = tx.run(statement, bindings);
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
