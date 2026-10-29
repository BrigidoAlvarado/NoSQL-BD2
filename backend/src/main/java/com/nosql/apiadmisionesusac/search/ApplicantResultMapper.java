package com.nosql.apiadmisionesusac.search;

import org.bson.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

/** Convierte documentos MongoDB a resultados HTTP sin ejecutar consultas. */
public final class ApplicantResultMapper {
    private ApplicantResultMapper() {}



    public static SearchResponse.EvaluationResult toSearchResult(Document d) {
        Document source = d.get("procedencia", Document.class);
        return new SearchResponse.EvaluationResult(
                d.getString("aspiranteId"),
                date(d.get("fechaAsignacion")),
                d.getString("materia"),
                integer(d, "oportunidad"),
                integer(d, "anioIngreso"),
                d.getString("aprobacion"),
                d.getString("carreraObjetivo"),
                nested(source, "tipoInstitucion"),
                nested(source, "municipio"),
                nested(source, "departamento"));
    }

    public static ApplicantDetail.Evaluation toDetail(Document d) {
        Document source = d.get("procedencia", Document.class);
        return new ApplicantDetail.Evaluation(
                date(d.get("fechaAsignacion")),
                d.getString("sexo"),
                d.getInteger("anioNacimiento"),
                d.getInteger("edadIngreso"),
                d.getString("materia"),
                integer(d, "oportunidad"),
                integer(d, "anioIngreso"),
                d.getString("aprobacion"),
                d.getString("carreraObjetivo"),
                nested(source, "tipoInstitucion"),
                nested(source, "municipio"),
                nested(source, "departamento"));
    }

    public static String nested(Document source, String key) {
        return source == null ? null : source.getString(key);
    }

    public static int integer(Document document, String key) {
        Number value = document.get(key, Number.class);
        return value == null ? 0 : value.intValue();
    }

    public static String date(Object value) {
        if (value instanceof Date date)
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .toString();
        if (value instanceof LocalDate date) return date.toString();
        return value == null ? null : value.toString();
    }
}
