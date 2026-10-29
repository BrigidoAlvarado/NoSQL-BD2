package com.nosql.apiadmisionesusac.statistics;

import org.bson.Document;
import java.util.Map;

/** Contratos de salida: porcentajes en escala 0–100 e intervalos en días. */
public final class AdditionalStatisticsResponse {
    private AdditionalStatisticsResponse() {}
    public record Opportunity(Map<String, Object> _id, long total, long aprobadas, long noAprobadas, double porcentaje) {}
    public record Recovery(Map<String, Object> _id, long conFallo, long recuperados, double porcentaje) {}
    public record Subjects(Map<String, Object> _id, long aspirantes, long totalCohorte, double porcentaje) {}
    public record Interval(Map<String, Object> _id, long incluidos, long excluidos, Double promedioDias, Double minimoDias, Double maximoDias) {}
    public record Quality(Map<String, Object> _id, long total, long sinEdad, long carreraDesconocida,
            long procedenciaDesconocida, double porcentajeSinEdad, double porcentajeCarreraDesconocida,
            double porcentajeProcedenciaDesconocida) {}

    public static Object from(String id, Document row) {
        Map<String, Object> group = row.get("_id", Document.class);
        return switch (id) {
            case "M21" -> new Opportunity(group, count(row,"total"), count(row,"aprobadas"), count(row,"noAprobadas"), number(row,"porcentaje"));
            case "M22" -> new Recovery(group, count(row,"conFallo"), count(row,"recuperados"), number(row,"porcentaje"));
            case "M23" -> new Subjects(group, count(row,"aspirantes"), count(row,"totalCohorte"), number(row,"porcentaje"));
            case "M24" -> new Interval(group, count(row,"incluidos"), count(row,"excluidos"), number(row,"promedioDias"), number(row,"minimoDias"), number(row,"maximoDias"));
            case "M25" -> new Quality(group, count(row,"total"), count(row,"sinEdad"), count(row,"carreraDesconocida"), count(row,"procedenciaDesconocida"), number(row,"porcentajeSinEdad"), number(row,"porcentajeCarreraDesconocida"), number(row,"porcentajeProcedenciaDesconocida"));
            default -> throw new IllegalArgumentException("Consulta desconocida");
        };
    }
    private static long count(Document row, String key) { return row.get(key, Number.class).longValue(); }
    private static Double number(Document row, String key) {
        Number value = row.get(key, Number.class);
        return value == null ? null : value.doubleValue();
    }
}
