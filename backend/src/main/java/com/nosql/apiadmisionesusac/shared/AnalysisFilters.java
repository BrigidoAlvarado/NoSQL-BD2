package com.nosql.apiadmisionesusac.shared;

import java.util.Map;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Parámetros permitidos; valores siempre literales, nunca operadores o texto de consulta. */
public final class AnalysisFilters {
    private AnalysisFilters() {}
    private static final Map<String, String> FIELDS = Map.of(
            "materia", "materia", "resultado", "aprobacion", "carrera", "carreraObjetivo",
            "departamento", "procedencia.departamento", "municipio", "procedencia.municipio",
            "tipoInstitucion", "procedencia.tipoInstitucion", "sexo", "sexo",
            "anio", "anioIngreso", "edad", "edadIngreso", "oportunidad", "oportunidad");
    public static Document mongo(Map<String, String> parameters) {
        Document filter = new Document();
        for (var entry : parameters.entrySet()) {
            if (entry.getKey().equals("mes")) {
                if (!entry.getValue().matches("\\d{4}-\\d{2}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes inválido.");
                filter.append("$expr", new Document("$eq", java.util.List.of(new Document("$dateToString", new Document("format", "%Y-%m").append("date", "$fechaAsignacion")), entry.getValue())));
                continue;
            }
            String field = FIELDS.get(entry.getKey());
            String value = entry.getValue();
            if (field == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro no permitido.");
            if (value == null || value.isEmpty()) continue;
            if (value.length() > 200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro demasiado largo.");
            Object literal = value;
            if (java.util.List.of("anio", "edad", "oportunidad").contains(entry.getKey())) {
                try { literal = Integer.valueOf(value); }
                catch (NumberFormatException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Filtro numérico inválido."); }
            }
            filter.append(field, literal);
        }
        return filter;
    }
}
