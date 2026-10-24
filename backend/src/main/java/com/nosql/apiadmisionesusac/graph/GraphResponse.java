package com.nosql.apiadmisionesusac.graph;

import java.util.List;
import java.util.Map;

public record GraphResponse(List<GraphNode> nodos, List<GraphEdge> relaciones, int evaluacionesIncluidas) {
    public record GraphNode(String id, String etiqueta, String titulo, Map<String, Object> propiedades) { }
    public record GraphEdge(String origen, String destino, String tipo) { }
}
