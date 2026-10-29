package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M06: Recalcula y guarda el resumen por carrera y cohorte. */
public final class M06CareerSummary {
    private M06CareerSummary() {}

    public static List<DashboardResponse.CareerSummary> execute(
            MongoCollection<Document> evaluations,
            com.nosql.apiadmisionesusac.statistics.summary.CareerSummaryStore store) {
        List<DashboardResponse.CareerSummary> result = new ArrayList<>();
        Document id = new Document("anio", "$anioIngreso").append("carrera", "$carreraObjetivo");
        List<Document> pipeline =
                List.of(
                        new Document(
                                "$group",
                                new Document("_id", id)
                                        .append("evaluaciones", new Document("$sum", 1))
                                        .append(
                                                "aspirantes",
                                                new Document("$addToSet", "$aspiranteId"))
                                        .append(
                                                "aprobadas",
                                                new Document("$sum", approvalExpression()))
                                        .append(
                                                "sinEdad",
                                                new Document(
                                                        "$sum",
                                                        absentExpression("$edadIngreso")))));
        for (Document document : aggregate(evaluations, pipeline)) {
            Document key = document.get("_id", Document.class);
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            DashboardResponse.CareerSummary row =
                    new DashboardResponse.CareerSummary(
                            (int) number(key.get("anio")),
                            label(key.get("carrera")),
                            total,
                            size(document.get("aspirantes")),
                            approved,
                            total - approved,
                            percentage(approved, total),
                            number(document.get("sinEdad")));
            result.add(row);
            store.save(row);
        }
        result.sort(
                Comparator.comparing(DashboardResponse.CareerSummary::anioIngreso)
                        .thenComparing(DashboardResponse.CareerSummary::carrera));
        return result;
    }
}
