package com.nosql.apiadmisionesusac.search;

import java.util.List;

public record ApplicantDetail(
        String aspiranteId,
        int totalEvaluaciones,
        int evaluacionesAprobadas,
        List<Evaluation> evaluaciones) {
    public record Evaluation(
            String fechaAsignacion,
            String sexo,
            Integer anioNacimiento,
            Integer edadIngreso,
            String materia,
            int oportunidad,
            int anioIngreso,
            String aprobacion,
            String carreraObjetivo,
            String tipoInstitucion,
            String municipio,
            String departamento) {}
}
