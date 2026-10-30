package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M09: Evaluaciones públicas por mes de asignación y materia. */
public final class M09PublicEvaluationsByMonth {
    private M09PublicEvaluationsByMonth() {}

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>();
        Document id =
                new Document(
                                "mes",
                                new Document(
                                        "$dateToString",
                                        new Document("format", "%Y-%m")
                                                .append("date", "$fechaAsignacion")))
                        .append("materia", "$materia");
        for (Document document :
                aggregate(evaluations, filter,
                        List.of(
                                new Document(
                                        "$match",
                                        new Document("procedencia.tipoInstitucion", "PUBLICO")),
                                new Document(
                                        "$group",
                                        new Document("_id", id)
                                                .append("cantidad", new Document("$sum", 1)))))) {
            Document key = document.get("_id", Document.class);
            rows.add(
                    new DashboardResponse.CountByPair(
                            label(key.get("mes")),
                            label(key.get("materia")),
                            number(document.get("cantidad"))));
        }
        rows.sort(
                Comparator.comparing(DashboardResponse.CountByPair::primeraEtiqueta)
                        .thenComparing(DashboardResponse.CountByPair::segundaEtiqueta));
        return rows;
    }
}
