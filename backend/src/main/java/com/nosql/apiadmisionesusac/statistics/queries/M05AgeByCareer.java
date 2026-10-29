package com.nosql.apiadmisionesusac.statistics.queries;

import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** M05: Una edad de referencia por aspirante y carrera. */
public final class M05AgeByCareer {
    private M05AgeByCareer() {}

    public static List<DashboardResponse.AverageByLabel> execute(
            MongoCollection<Document> evaluations) {
        List<DashboardResponse.AverageByLabel> rows = new ArrayList<>();
        List<Document> pipeline =
                AgeQuerySupport.firstAgePipeline(null, new Document("carrera", "$_id.carrera"));
        for (Document document : aggregate(evaluations, pipeline))
            rows.add(AgeQuerySupport.averageLabel(document));
        rows.sort(Comparator.comparing(DashboardResponse.AverageByLabel::etiqueta));
        return rows;
    }
}
