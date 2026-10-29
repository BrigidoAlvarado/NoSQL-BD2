package com.nosql.apiadmisionesusac.search.queries;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/** M11: todas las evaluaciones del correlativo, ordenadas cronológicamente. */
public final class M11ApplicantHistory {
    private M11ApplicantHistory() {}

    public static List<Document> execute(MongoCollection<Document> collection, String applicantId) {
        return collection
                .find(Filters.eq("aspiranteId", applicantId))
                .sort(
                        Sorts.orderBy(
                                Sorts.ascending("anioIngreso"),
                                Sorts.ascending("fechaAsignacion"),
                                Sorts.ascending("materia"),
                                Sorts.ascending("oportunidad")))
                .into(new ArrayList<>());
    }
}
