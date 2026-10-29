package com.nosql.apiadmisionesusac.statistics;

import org.bson.Document;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/** Cuatro evaluaciones: dos personas, dos carreras y dos aprobaciones. */
final class QueryFixture {
    private QueryFixture() {}

    static List<Document> evaluations() {
        return List.of(
                evaluation("e1", "a1", "MATEMATICA", 1, "NO APROBADO", "A", "PUBLICO", 18),
                evaluation("e2", "a1", "MATEMATICA", 2, "APROBADO", "A", "PUBLICO", 18),
                evaluation("e3", "a1", "FISICA", 1, "APROBADO", "B", "PUBLICO", 18),
                evaluation("e4", "a2", "MATEMATICA", 1, "NO APROBADO", "A", "PRIVADO", 20));
    }

    private static Document evaluation(
            String id,
            String applicant,
            String subject,
            int opportunity,
            String result,
            String career,
            String type,
            int age) {
        Document source =
                new Document("departamento", "GUATEMALA")
                        .append("municipio", "MIXCO")
                        .append("tipoInstitucion", type)
                        .append("departamentoConocido", true)
                        .append("municipioConocido", true);
        return new Document("_id", id)
                .append("aspiranteId", applicant)
                .append("materia", subject)
                .append("oportunidad", opportunity)
                .append("aprobacion", result)
                .append("carreraObjetivo", career)
                .append("carreraObjetivoConocida", true)
                .append("edadIngreso", age)
                .append("anioNacimiento", 2022 - age)
                .append("anioIngreso", 2022)
                .append("sexo", applicant.equals("a1") ? "F" : "M")
                .append("fechaAsignacion", Date.from(Instant.parse("2021-08-01T00:00:00Z")))
                .append("procedencia", source);
    }
}
