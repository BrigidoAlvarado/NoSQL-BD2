package com.nosql.apiadmisionesusac.upload.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import com.nosql.apiadmisionesusac.upload.LoadHistoryService;

/** DTO de presentación; no expone campos internos nuevos de MongoDB por accidente. */
public final class LoadHistoryResponse {
    private LoadHistoryResponse() {}
    public record Page(List<Object> items, long total, int page, int size) {}
    public record Load(String sha256Archivo, String nombreArchivo, String estado, Date iniciadaEn,
            Date actualizadaEn, Integer filasLeidas, Integer filasValidas, Integer filasRechazadas, String mensaje) {}
    public record Rejection(Integer fila, String codigo, String detalle, Map<String, Object> valores) {}
    public record Source(String sha256Archivo, String nombreArchivo, Integer fila) {}
    public record Evaluation(String aspiranteId, Date fechaAsignacion, String materia, Integer oportunidad,
            Integer anioIngreso, String aprobacion, String carreraObjetivo, Integer anioNacimiento,
            Integer edadIngreso, Map<String, Object> procedencia, List<Source> fuentes) {}

    public static Page from(LoadHistoryService.Page page, String kind) {
        return new Page(page.items().stream().map(row -> convert(row, kind)).toList(), page.total(), page.page(), page.size());
    }
    private static Object convert(Document row, String kind) {
        if (kind.equals("cargas")) return new Load(row.getString("sha256Archivo"), row.getString("nombreArchivo"),
                row.getString("estado"), row.getDate("iniciadaEn"), row.getDate("actualizadaEn"),
                integer(row,"filasLeidas"), integer(row,"filasValidas"), integer(row,"filasRechazadas"), row.getString("mensaje"));
        if (kind.equals("rechazos")) return new Rejection(integer(row,"fila"), row.getString("codigo"),
                row.getString("detalle"), row.get("valores", Document.class));
        var sources = row.getList("fuentes", Document.class).stream().map(source -> new Source(
                source.getString("sha256Archivo"), source.getString("nombreArchivo"), integer(source,"fila"))).toList();
        return new Evaluation(row.getString("aspiranteId"), row.getDate("fechaAsignacion"), row.getString("materia"),
                integer(row,"oportunidad"), integer(row,"anioIngreso"), row.getString("aprobacion"),
                row.getString("carreraObjetivo"), integer(row,"anioNacimiento"), integer(row,"edadIngreso"),
                row.get("procedencia", Document.class), sources);
    }
    private static Integer integer(Document row, String key) {
        Number value = row.get(key, Number.class);
        return value == null ? null : value.intValue();
    }
}
