package com.nosql.apiadmisionesusac.statistics.summary;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.ReplaceOptions;
import com.nosql.apiadmisionesusac.statistics.DashboardResponse.CareerSummary;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/** Guarda el resultado de M06; no calcula estadísticas. */
@Repository
public class CareerSummaryStore {
    private final MongoTemplate mongo;

    public CareerSummaryStore(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public void save(CareerSummary row) {
        String id = row.anioIngreso() + "\u001F" + row.carrera();
        Document document =
                new Document("_id", id)
                        .append("anioIngreso", row.anioIngreso())
                        .append("carreraObjetivo", row.carrera())
                        .append("evaluaciones", row.evaluaciones())
                        .append("aspirantesDistintos", row.aspirantesDistintos())
                        .append("aprobadas", row.aprobadas())
                        .append("noAprobadas", row.noAprobadas())
                        .append("porcentajeAprobacion", row.porcentajeAprobacion())
                        .append("evaluacionesSinEdad", row.sinEdad());
        mongo.getCollection("resumen_carrera")
                .replaceOne(eq("_id", id), document, new ReplaceOptions().upsert(true));
    }
}
