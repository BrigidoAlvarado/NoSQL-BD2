package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M17: Cinco municipios conocidos con más aspirantes. */
public final class M17TopMunicipalities {
    private M17TopMunicipalities() {}

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>();
        Document id =
                new Document("departamento", "$procedencia.departamento")
                        .append("municipio", "$procedencia.municipio");
        Document match =
                new Document("procedencia.departamentoConocido", true)
                        .append("procedencia.municipioConocido", true);
        for (Document document :
                aggregate(
                        evaluations,
                        List.of(
                                new Document("$match", match),
                                new Document(
                                        "$group",
                                        new Document("_id", id)
                                                .append(
                                                        "aspirantes",
                                                        new Document(
                                                                "$addToSet", "$aspiranteId")))))) {
            Document key = document.get("_id", Document.class);
            rows.add(
                    new DashboardResponse.CountByPair(
                            label(key.get("departamento")),
                            label(key.get("municipio")),
                            size(document.get("aspirantes"))));
        }
        rows.sort(
                Comparator.comparing(DashboardResponse.CountByPair::cantidad)
                        .reversed()
                        .thenComparing(DashboardResponse.CountByPair::segundaEtiqueta));
        return rows.stream().limit(5).toList();
    }
}
