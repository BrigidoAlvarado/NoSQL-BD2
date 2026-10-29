package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** Regla común de M05/M07: una edad de la primera cohorte por persona y grupo. */
final class AgeQuerySupport {
    private AgeQuerySupport() {}

    static List<Document> firstAgePipeline(Document match, Document finalId) {
        List<Document> pipeline = new ArrayList<>();
        if (match != null) pipeline.add(new Document("$match", match));
        pipeline.add(
                new Document("$sort", new Document("anioIngreso", 1).append("fechaAsignacion", 1)));
        Document person =
                new Document("aspirante", "$aspiranteId").append("carrera", "$carreraObjetivo");
        if (match != null) person.append("tipo", "$procedencia.tipoInstitucion");
        pipeline.add(
                new Document(
                        "$group",
                        new Document("_id", person)
                                .append("edad", new Document("$first", "$edadIngreso"))));
        pipeline.add(
                new Document(
                        "$group",
                        new Document("_id", finalId)
                                .append("promedio", new Document("$avg", "$edad"))
                                .append("conDato", new Document("$sum", presentExpression("$edad")))
                                .append(
                                        "sinDato",
                                        new Document("$sum", absentExpression("$edad")))));
        return pipeline;
    }

    static DashboardResponse.AverageByLabel averageLabel(Document document) {
        Document group = document.get("_id", Document.class);
        return new DashboardResponse.AverageByLabel(
                label(group.get("carrera")),
                decimal(document.get("promedio")),
                number(document.get("conDato")),
                number(document.get("sinDato")));
    }
}
