package com.nosql.apiadmisionesusac.search;

import java.util.List;

public record SearchResponse(String criterio, String termino, int totalDevuelto, List<EvaluationResult> resultados) {
    public record EvaluationResult(String aspiranteId, String fechaAsignacion, String materia, int oportunidad,
                                   int anioIngreso, String aprobacion, String carreraObjetivo,
                                   String tipoInstitucion, String municipio, String departamento) { }
}
