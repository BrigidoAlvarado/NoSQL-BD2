package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M14: Oportunidades distintas por aspirante, cohorte y materia. */
public final class M14OpportunitiesBySubject {
    private M14OpportunitiesBySubject() {}

    public static List<DashboardResponse.AverageOpportunities> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.AverageOpportunities> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.AverageOpportunities> rows = new ArrayList<>();
        Document opportunity =
                new Document("aspirante", "$aspiranteId")
                        .append("anio", "$anioIngreso")
                        .append("materia", "$materia")
                        .append("oportunidad", "$oportunidad");
        Document group =
                new Document("aspirante", "$_id.aspirante")
                        .append("anio", "$_id.anio")
                        .append("materia", "$_id.materia");
        List<Document> pipeline =
                List.of(
                        new Document("$group", new Document("_id", opportunity)),
                        new Document(
                                "$group",
                                new Document("_id", group)
                                        .append("usadas", new Document("$sum", 1))),
                        new Document(
                                "$group",
                                new Document("_id", "$_id.materia")
                                        .append("promedio", new Document("$avg", "$usadas"))
                                        .append("grupos", new Document("$sum", 1))));
        for (Document document : aggregate(evaluations, filter, pipeline))
            rows.add(
                    new DashboardResponse.AverageOpportunities(
                            label(document.get("_id")),
                            decimal(document.get("promedio")),
                            number(document.get("grupos"))));
        rows.sort(Comparator.comparing(DashboardResponse.AverageOpportunities::materia));
        return rows;
    }
}
