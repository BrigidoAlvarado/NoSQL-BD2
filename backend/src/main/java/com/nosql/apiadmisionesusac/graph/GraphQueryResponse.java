package com.nosql.apiadmisionesusac.graph;

import java.util.List;
import java.util.Map;

public record GraphQueryResponse(
        String id,
        String titulo,
        String descripcion,
        List<String> columnas,
        List<Map<String, Object>> filas) {}
