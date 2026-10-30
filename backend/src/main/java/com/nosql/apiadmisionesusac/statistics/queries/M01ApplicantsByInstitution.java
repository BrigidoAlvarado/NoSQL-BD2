package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** M01: Correlativos distintos por tipo de institución. */
public final class M01ApplicantsByInstitution {
    private M01ApplicantsByInstitution() {}

    public static List<DashboardResponse.CountByLabel> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.CountByLabel> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.CountByLabel> rows = new ArrayList<>();
        for (Document document :
                aggregate(evaluations, filter,
                        List.of(
                                new Document(
                                        "$group",
                                        new Document("_id", "$procedencia.tipoInstitucion")
                                                .append(
                                                        "aspirantes",
                                                        new Document(
                                                                "$addToSet", "$aspiranteId")))))) {
            rows.add(
                    new DashboardResponse.CountByLabel(
                            label(document.get("_id")), size(document.get("aspirantes"))));
        }
        rows.sort(byCount());
        return rows;
    }
}
