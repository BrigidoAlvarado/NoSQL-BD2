package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M13: Porcentaje de aprobación por edad válida. */
public final class M13ApprovalByAge {
    private M13ApprovalByAge() {}

    public static List<DashboardResponse.AgeRate> execute(MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.AgeRate> execute(MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.AgeRate> rows = new ArrayList<>();
        for (Document document :
                aggregate(evaluations, filter,
                        List.of(
                                new Document(
                                        "$match",
                                        new Document("edadIngreso", new Document("$ne", null))),
                                new Document(
                                        "$group",
                                        new Document("_id", "$edadIngreso")
                                                .append("evaluaciones", new Document("$sum", 1))
                                                .append(
                                                        "aprobadas",
                                                        new Document(
                                                                "$sum", approvalExpression())))))) {
            long total = number(document.get("evaluaciones")),
                    approved = number(document.get("aprobadas"));
            rows.add(
                    new DashboardResponse.AgeRate(
                            (int) number(document.get("_id")),
                            approved,
                            total,
                            percentage(approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.AgeRate::edad));
        return rows;
    }
}
