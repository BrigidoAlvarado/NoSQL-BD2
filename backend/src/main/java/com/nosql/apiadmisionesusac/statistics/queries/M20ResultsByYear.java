package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M20: Evaluaciones y aprobación por cohorte. */
public final class M20ResultsByYear {
    private M20ResultsByYear() {}

    public static List<DashboardResponse.ResultsByYear> execute(
            MongoCollection<Document> evaluations) {
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
        for (Document document : aggregate(evaluations, pipeline)) {
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            rows.add(
                    new DashboardResponse.ResultsByYear(
                            (int) number(document.get("_id")),
                            approved,
                            total - approved,
                            total,
                            percentage(approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.ResultsByYear::anioIngreso));
        return rows;
    }
}
