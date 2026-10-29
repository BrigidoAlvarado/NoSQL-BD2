package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** M02: Evaluaciones aprobadas por materia. */
public final class M02ApprovedBySubject {
    private M02ApprovedBySubject() {}

    public static List<DashboardResponse.CountByLabel> execute(
            MongoCollection<Document> evaluations) {
        List<DashboardResponse.CountByLabel> rows = new ArrayList<>();
        List<Document> pipeline =
                List.of(
                        new Document("$match", new Document("aprobacion", "APROBADO")),
                        new Document(
                                "$group",
                                new Document("_id", "$materia")
                                        .append("cantidad", new Document("$sum", 1))));
        for (Document document : aggregate(evaluations, pipeline)) {
            rows.add(
                    new DashboardResponse.CountByLabel(
                            label(document.get("_id")), number(document.get("cantidad"))));
        }
        rows.sort(byCount());
        return rows;
    }
}
