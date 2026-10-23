package com.nosql.apiadmisionesusac.statistics;

import java.util.List;

/** Resultados agregados de MongoDB para el primer panel de estadísticas. */
public record DashboardResponse(
        Summary resumen,
        List<CountByLabel> aspirantesPorInstitucion,
        List<CountByLabel> aprobadasPorMateria,
        List<RateByLabel> aprobacionPorMateria,
        List<CountByPair> aspirantesPorSexoInstitucion,
        List<RateByLabel> aprobacionPorInstitucion,
        List<ResultsByYear> resultadosPorAnio) {

    public record Summary(long evaluaciones, long aspirantesUnicos, long aprobadas, double porcentajeAprobacion) {
    }

    public record CountByLabel(String etiqueta, long cantidad) {
    }

    public record RateByLabel(String etiqueta, long aprobadas, long evaluaciones, double porcentajeAprobacion) {
    }

    public record CountByPair(String primeraEtiqueta, String segundaEtiqueta, long cantidad) {
    }

    public record ResultsByYear(int anioIngreso, long aprobadas, long noAprobadas, long evaluaciones,
                                double porcentajeAprobacion) {
    }
}
