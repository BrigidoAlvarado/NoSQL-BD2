package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M20: Cantidades y porcentajes de ambos resultados sobre las evaluaciones de cada cohorte. */
public final class M20ResultsByYear {
    private M20ResultsByYear() {}

    public static List<DashboardResponse.ResultsByYear> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.ResultsByYear> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.ResultsByYear> rows = new ArrayList<>();
        List<Document> pipeline =
                List.of(
                        new Document(
                                "$group",
                                new Document("_id", "$anioIngreso")
                                        .append("evaluaciones", new Document("$sum", 1))
                                        .append(
                                                "aprobadas",
                                                new Document("$sum", approvalExpression()))));
        for (Document document : aggregate(evaluations, filter, pipeline)) {
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            rows.add(
                    new DashboardResponse.ResultsByYear(
                            (int) number(document.get("_id")),
                            approved,
                            total - approved,
                            total,
                            percentage(approved, total),
                            percentage(total - approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.ResultsByYear::anioIngreso));
        return rows;
    }
}
