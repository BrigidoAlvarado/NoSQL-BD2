package com.nosql.apiadmisionesusac.graph;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Convierte registros del explorador en nodos y aristas sin acceder a Neo4j. */
final class GraphResponseMapper {
    GraphResponse map(List<Record> records) {
        Map<String, GraphResponse.GraphNode> nodes = new LinkedHashMap<>();
        Set<GraphResponse.GraphEdge> edges = new LinkedHashSet<>();
        for (Record record : records) {
            Node a = record.get("a").asNode(),
                    e = record.get("e").asNode(),
                    c = record.get("c").asNode();
            Node m = record.get("m").asNode(),
                    r = record.get("r").asNode(),
                    p = record.get("p").asNode();
            Node u = record.get("u").asNode(), d = record.get("d").asNode();
            add(nodes, a);
            add(nodes, e);
            add(nodes, c);
            add(nodes, m);
            add(nodes, r);
            add(nodes, p);
            add(nodes, u);
            add(nodes, d);
            edge(edges, a, e, "REALIZO");
            edge(edges, e, c, "PARA_CARRERA");
            edge(edges, e, m, "EVALUO_MATERIA");
            edge(edges, e, r, "OBTUVO_RESULTADO");
            edge(edges, e, p, "TIENE_PROCEDENCIA");
            edge(edges, p, u, "UBICADA_EN");
            edge(edges, u, d, "PERTENECE_A");
        }
        return new GraphResponse(List.copyOf(nodes.values()), List.copyOf(edges), records.size());
    }

    private void add(Map<String, GraphResponse.GraphNode> nodes, Node node) {
        String label = node.labels().iterator().next();
        String id = nodeId(label, node);
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String key :
                List.of(
                        "anioIngreso",
                        "aprobacion",
                        "sexo",
                        "oportunidad",
                        "conocida",
                        "conocido",
                        "nombre",
                        "departamento",
                        "municipio",
                        "tipo"))
            if (node.containsKey(key)) properties.put(key, node.get(key).asObject());
        nodes.putIfAbsent(
                id, new GraphResponse.GraphNode(id, label, title(label, node), properties));
    }

    private void edge(Set<GraphResponse.GraphEdge> edges, Node from, Node to, String type) {
        edges.add(
                new GraphResponse.GraphEdge(
                        nodeId(from.labels().iterator().next(), from),
                        nodeId(to.labels().iterator().next(), to),
                        type));
    }

    private String nodeId(String label, Node node) {
        String key =
                node.containsKey("id")
                        ? node.get("id").asString()
                        : node.containsKey("clave")
                                ? node.get("clave").asString()
                                : node.get("codigo").asString();
        return label + ":" + key;
    }

    private String title(String label, Node node) {
        return switch (label) {
            case "Aspirante", "Evaluacion" ->
                    label
                            + " "
                            + node.get("id")
                                    .asString()
                                    .substring(0, Math.min(10, node.get("id").asString().length()));
            case "Municipio" -> node.get("nombre").asString();
            case "ProcedenciaEducativa" ->
                    node.get("tipo").asString() + " · " + node.get("municipio").asString();
            default ->
                    node.containsKey("clave")
                            ? node.get("clave").asString()
                            : node.get("codigo").asString();
        };
    }
}
