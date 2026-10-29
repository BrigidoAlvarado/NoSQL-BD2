package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** M19: Carreras más demandadas por departamento, incluyendo empates. */
public final class M19CareersByDepartment {
    private M19CareersByDepartment() {}

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations) {
        Map<String, List<DashboardResponse.CountByPair>> grouped = new LinkedHashMap<>();
        Document id =
                new Document("departamento", "$procedencia.departamento")
                        .append("carrera", "$carreraObjetivo");
        Document match =
                new Document("procedencia.departamentoConocido", true)
                        .append("carreraObjetivoConocida", true);
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
            String dept = label(key.get("departamento"));
            grouped.computeIfAbsent(dept, k -> new ArrayList<>())
                    .add(
                            new DashboardResponse.CountByPair(
                                    dept,
                                    label(key.get("carrera")),
                                    size(document.get("aspirantes"))));
        }
        List<DashboardResponse.CountByPair> result = new ArrayList<>();
        for (List<DashboardResponse.CountByPair> choices : grouped.values()) {
            long max =
                    choices.stream()
                            .mapToLong(DashboardResponse.CountByPair::cantidad)
                            .max()
                            .orElse(0);
            choices.stream().filter(c -> c.cantidad() == max).forEach(result::add);
        }
        result.sort(
                Comparator.comparing(DashboardResponse.CountByPair::primeraEtiqueta)
                        .thenComparing(DashboardResponse.CountByPair::segundaEtiqueta));
        return result;
    }
}
