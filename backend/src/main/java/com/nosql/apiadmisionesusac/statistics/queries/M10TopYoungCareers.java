package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** M10: Cinco carreras conocidas más demandadas entre 16 y 18 años. */
public final class M10TopYoungCareers {
    private M10TopYoungCareers() {}

    public static List<DashboardResponse.CountByLabel> execute(
            MongoCollection<Document> evaluations) {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$sort", new Document("anioIngreso", 1)));
        pipeline.add(
                new Document(
                        "$group",
                        new Document(
                                        "_id",
                                        new Document("aspirante", "$aspiranteId")
                                                .append("carrera", "$carreraObjetivo"))
                                .append("edad", new Document("$first", "$edadIngreso"))
                                .append(
                                        "conocida",
                                        new Document("$first", "$carreraObjetivoConocida"))));
        pipeline.add(
                new Document(
                        "$match",
                        new Document("edad", new Document("$gte", 16).append("$lte", 18))
                                .append("conocida", true)));
        pipeline.add(
                new Document(
                        "$group",
                        new Document("_id", "$_id.carrera")
                                .append("cantidad", new Document("$sum", 1))));
        return countRows(evaluations, pipeline).stream().limit(5).toList();
    }
}
