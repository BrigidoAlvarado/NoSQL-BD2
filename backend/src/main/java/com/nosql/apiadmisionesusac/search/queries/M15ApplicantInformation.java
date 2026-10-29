package com.nosql.apiadmisionesusac.search.queries;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.search.ApplicantDetail;
import com.nosql.apiadmisionesusac.search.ApplicantResultMapper;

import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** M15: información completa y totales del aspirante, usando su historial M11. */
public final class M15ApplicantInformation {
    private M15ApplicantInformation() {}

    public static ApplicantDetail execute(
            MongoCollection<Document> collection, String applicantId) {
        List<ApplicantDetail.Evaluation> evaluations =
                M11ApplicantHistory.execute(collection, applicantId).stream()
                        .map(ApplicantResultMapper::toDetail)
                        .toList();
        if (evaluations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No se encontró el aspirante solicitado.");
        }
        int approved =
                (int)
                        evaluations.stream()
                                .filter(row -> "APROBADO".equals(row.aprobacion()))
                                .count();
        return new ApplicantDetail(applicantId, evaluations.size(), approved, evaluations);
    }
}
