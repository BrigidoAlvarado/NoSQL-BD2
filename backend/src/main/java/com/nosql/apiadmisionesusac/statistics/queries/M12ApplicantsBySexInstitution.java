package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M12: Aspirantes distintos por sexo y tipo de institución. */
public final class M12ApplicantsBySexInstitution {
    private M12ApplicantsBySexInstitution() {}

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations) {
        return execute(evaluations, new Document());
    }

    public static List<DashboardResponse.CountByPair> execute(
            MongoCollection<Document> evaluations, Document filter) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>();
        Document groupId =
                new Document("sexo", "$sexo").append("tipo", "$procedencia.tipoInstitucion");
        List<Document> pipeline =
                List.of(
                        new Document(
                                "$group",
                                new Document("_id", groupId)
                                        .append(
                                                "aspirantes",
                                                new Document("$addToSet", "$aspiranteId"))));
        for (Document document : aggregate(evaluations, filter, pipeline)) {
            Document id = document.get("_id", Document.class);
            rows.add(
                    new DashboardResponse.CountByPair(
                            label(id.get("sexo")),
                            label(id.get("tipo")),
                            size(document.get("aspirantes"))));
        }
        rows.sort(
                Comparator.comparing(DashboardResponse.CountByPair::segundaEtiqueta)
                        .thenComparing(DashboardResponse.CountByPair::primeraEtiqueta));
        return rows;
    }
}
