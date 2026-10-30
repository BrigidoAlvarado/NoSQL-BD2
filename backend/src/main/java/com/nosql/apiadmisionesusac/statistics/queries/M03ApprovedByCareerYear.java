package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M03: Aspirantes aprobados por carrera y cohorte. */
public final class M03ApprovedByCareerYear {
    private M03ApprovedByCareerYear() {}

    public static List<DashboardResponse.CareerYearCount> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.CareerYearCount> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.CareerYearCount> rows = new ArrayList<>();
        Document id = new Document("carrera", "$carreraObjetivo").append("anio", "$anioIngreso");
        for (Document document :
                aggregate(evaluations, filter,
                        List.of(
                                new Document("$match", new Document("aprobacion", "APROBADO")),
                                new Document(
                                        "$group",
                                        new Document("_id", id)
                                                .append(
                                                        "aspirantes",
                                                        new Document(
                                                                "$addToSet", "$aspiranteId")))))) {
            Document key = document.get("_id", Document.class);
            rows.add(
                    new DashboardResponse.CareerYearCount(
                            label(key.get("carrera")),
                            (int) number(key.get("anio")),
                            size(document.get("aspirantes"))));
        }
        rows.sort(
                Comparator.comparing(DashboardResponse.CareerYearCount::anioIngreso)
                        .thenComparing(DashboardResponse.CareerYearCount::carrera));
        return rows;
    }
}
