package com.nosql.apiadmisionesusac.statistics;

import static com.mongodb.client.model.Filters.eq;
import static com.nosql.apiadmisionesusac.statistics.queries.QueryValues.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.statistics.queries.*;
import com.nosql.apiadmisionesusac.statistics.summary.CareerSummaryStore;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoStatisticsService {
    private static final String EVALUACIONES = "evaluaciones";
    private final MongoTemplate mongoTemplate;

    private final CareerSummaryStore summaries;

    public MongoStatisticsService(MongoTemplate mongoTemplate, CareerSummaryStore summaries) {
        this.summaries = summaries;
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardResponse dashboard() {
        MongoCollection<Document> evaluations = mongoTemplate.getCollection(EVALUACIONES);
        long total = evaluations.countDocuments();
        long approved = evaluations.countDocuments(eq("aprobacion", "APROBADO"));
        long applicants =
                aggregate(
                                evaluations,
                                List.of(
                                        new Document(
                                                "$group", new Document("_id", "$aspiranteId"))))
                        .size();
        return new DashboardResponse(
                new DashboardResponse.Summary(
                        total, applicants, approved, percentage(approved, total)),
                M01ApplicantsByInstitution.execute(evaluations),
                M02ApprovedBySubject.execute(evaluations),
                M04ApprovalBySubject.execute(evaluations),
                M12ApplicantsBySexInstitution.execute(evaluations),
                M18ApprovalByInstitution.execute(evaluations),
                M20ResultsByYear.execute(evaluations),
                M03ApprovedByCareerYear.execute(evaluations),
                M05AgeByCareer.execute(evaluations),
                M06CareerSummary.execute(evaluations, summaries),
                M07ApprovedAgeByInstitution.execute(evaluations),
                M08ApprovedByMunicipality.execute(evaluations),
                M09PublicEvaluationsByMonth.execute(evaluations),
                M10TopYoungCareers.execute(evaluations),
                M13ApprovalByAge.execute(evaluations),
                M14OpportunitiesBySubject.execute(evaluations),
                M16FirstAttemptFailures.execute(evaluations),
                M17TopMunicipalities.execute(evaluations),
                M19CareersByDepartment.execute(evaluations));
    }
}
