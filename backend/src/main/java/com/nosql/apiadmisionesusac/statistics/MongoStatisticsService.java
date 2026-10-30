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

    public DashboardResponse dashboard() { return dashboard(new Document()); }

    public DashboardResponse dashboard(Document filter) {
        MongoCollection<Document> evaluations = mongoTemplate.getCollection(EVALUACIONES);
        long total = evaluations.countDocuments(filter);
        long approved = evaluations.countDocuments(com.mongodb.client.model.Filters.and(filter, eq("aprobacion", "APROBADO")));
        long applicants =
                aggregate(evaluations, filter,
                                List.of(
                                        new Document(
                                                "$group", new Document("_id", "$aspiranteId"))))
                        .size();
        return new DashboardResponse(
                new DashboardResponse.Summary(
                        total, applicants, approved, percentage(approved, total)),
                M01ApplicantsByInstitution.execute(evaluations, filter),
                M02ApprovedBySubject.execute(evaluations, filter),
                M04ApprovalBySubject.execute(evaluations, filter),
                M12ApplicantsBySexInstitution.execute(evaluations, filter),
                M18ApprovalByInstitution.execute(evaluations, filter),
                M20ResultsByYear.execute(evaluations, filter),
                M03ApprovedByCareerYear.execute(evaluations, filter),
                M05AgeByCareer.execute(evaluations, filter),
                M06CareerSummary.execute(evaluations, summaries, filter),
                M07ApprovedAgeByInstitution.execute(evaluations, filter),
                M08ApprovedByMunicipality.execute(evaluations, filter),
                M09PublicEvaluationsByMonth.execute(evaluations, filter),
                M10TopYoungCareers.execute(evaluations, filter),
                M13ApprovalByAge.execute(evaluations, filter),
                M14OpportunitiesBySubject.execute(evaluations, filter),
                M16FirstAttemptFailures.execute(evaluations, filter),
                M17TopMunicipalities.execute(evaluations, filter),
                M19CareersByDepartment.execute(evaluations, filter));
    }
}
