package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.List;

/** M16: Aspirantes que no aprobaron en oportunidad calendario uno. */
public final class M16FirstAttemptFailures {
    private M16FirstAttemptFailures() {}

    public static List<DashboardResponse.CountByLabel> execute(
            MongoCollection<Document> evaluations) {
        return countRows(
                evaluations,
                List.of(
                        new Document(
                                "$match",
                                new Document("oportunidad", 1)
                                        .append("aprobacion", "NO APROBADO")
                                        .append("carreraObjetivoConocida", true)),
                        new Document(
                                "$group",
                                new Document("_id", "$carreraObjetivo")
                                        .append(
                                                "aspirantes",
                                                new Document("$addToSet", "$aspiranteId")))),
                "aspirantes");
    }
}
