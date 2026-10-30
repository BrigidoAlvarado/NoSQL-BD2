package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M08: Aprobados por departamento, municipio y carrera. */
public final class M08ApprovedByMunicipality {
    private M08ApprovedByMunicipality() {}

    public static List<DashboardResponse.CountByTriple> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.CountByTriple> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.CountByTriple> rows = new ArrayList<>();
        Document id =
                new Document("departamento", "$procedencia.departamento")
                        .append("municipio", "$procedencia.municipio")
                        .append("carrera", "$carreraObjetivo");
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
                    new DashboardResponse.CountByTriple(
                            label(key.get("departamento")),
                            label(key.get("municipio")),
                            label(key.get("carrera")),
                            size(document.get("aspirantes"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.CountByTriple::cantidad).reversed());
        return rows;
    }
}
