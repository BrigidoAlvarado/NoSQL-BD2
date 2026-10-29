package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M18: Porcentaje de aprobación por tipo de institución. */
public final class M18ApprovalByInstitution {
    private M18ApprovalByInstitution() {}

    public static List<DashboardResponse.RateByLabel> execute(
            MongoCollection<Document> evaluations) {
        List<DashboardResponse.RateByLabel> rows = new ArrayList<>();
        List<Document> pipeline =
                List.of(
                        new Document(
                                "$group",
                                new Document("_id", "$procedencia.tipoInstitucion")
                                        .append("evaluaciones", new Document("$sum", 1))
                                        .append(
                                                "aprobadas",
                                                new Document("$sum", approvalExpression()))));
        for (Document document : aggregate(evaluations, pipeline)) {
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            rows.add(
                    new DashboardResponse.RateByLabel(
                            label(document.get("_id")),
                            approved,
                            total,
                            percentage(approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.RateByLabel::etiqueta));
        return rows;
    }
}
