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
        List<ResultsByYear> resultadosPorAnio,
        List<CareerYearCount> aprobadosPorCarreraAnio,
        List<AverageByLabel> edadPorCarrera,
        List<CareerSummary> resumenCarreras,
        List<AverageByPair> edadAprobadosPorCarreraInstitucion,
        List<CountByTriple> aprobadosPorMunicipioCarrera,
        List<CountByPair> evaluacionesPublicasPorMesMateria,
        List<CountByLabel> topCarrerasJuveniles,
        List<AgeRate> aprobacionPorEdad,
        List<AverageOpportunities> oportunidadesPorMateria,
        List<CountByLabel> noAprobaronPrimerIntentoPorCarrera,
        List<CountByPair> topMunicipios,
        List<CountByPair> carrerasMasDemandadasPorDepartamento) {

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

    public record CareerYearCount(String carrera, int anioIngreso, long aspirantes) { }
    public record AverageByLabel(String etiqueta, double promedio, long conDato, long sinDato) { }
    public record AverageByPair(String primeraEtiqueta, String segundaEtiqueta, double promedio, long conDato, long sinDato) { }
    public record CountByTriple(String primeraEtiqueta, String segundaEtiqueta, String terceraEtiqueta, long cantidad) { }
    public record AgeRate(int edad, long aprobadas, long evaluaciones, double porcentajeAprobacion) { }
    public record AverageOpportunities(String materia, double promedio, long grupos) { }
    public record CareerSummary(int anioIngreso, String carrera, long evaluaciones, long aspirantesDistintos,
                                long aprobadas, long noAprobadas, double porcentajeAprobacion, long sinEdad) { }
}
