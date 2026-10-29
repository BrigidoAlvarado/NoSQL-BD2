package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M07: Edad de aprobados por carrera y tipo de institución. */
public final class M07ApprovedAgeByInstitution {
    private M07ApprovedAgeByInstitution() {}

    public static List<DashboardResponse.AverageByPair> execute(
            MongoCollection<Document> evaluations) {
        List<DashboardResponse.AverageByPair> rows = new ArrayList<>();
        Document grouped = new Document("carrera", "$_id.carrera").append("tipo", "$_id.tipo");
        for (Document document :
                aggregate(
                        evaluations,
                        AgeQuerySupport.firstAgePipeline(
                                new Document("aprobacion", "APROBADO"), grouped))) {
            Document id = document.get("_id", Document.class);
            rows.add(
                    new DashboardResponse.AverageByPair(
                            label(id.get("carrera")),
                            label(id.get("tipo")),
                            decimal(document.get("promedio")),
                            number(document.get("conDato")),
                            number(document.get("sinDato"))));
        }
        rows.sort(
                Comparator.comparing(DashboardResponse.AverageByPair::primeraEtiqueta)
                        .thenComparing(DashboardResponse.AverageByPair::segundaEtiqueta));
        return rows;
    }
}
