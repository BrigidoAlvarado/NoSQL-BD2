package com.nosql.apiadmisionesusac.statistics;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoCollection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class MongoStatisticsService {
    private static final String EVALUACIONES = "evaluaciones";
    private final MongoTemplate mongoTemplate;

    public MongoStatisticsService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public DashboardResponse dashboard() {
        MongoCollection<Document> evaluations = mongoTemplate.getCollection(EVALUACIONES);
        long total = evaluations.countDocuments();
        long approved = evaluations.countDocuments(eq("aprobacion", "APROBADO"));
        long applicants = aggregate(evaluations, List.of(new Document("$group", new Document("_id", "$aspiranteId")))).size();
        return new DashboardResponse(
                new DashboardResponse.Summary(total, applicants, approved, percentage(approved, total)),
                applicantsByInstitution(evaluations), approvedBySubject(evaluations), approvalBySubject(evaluations),
                applicantsBySexAndInstitution(evaluations), approvalByInstitution(evaluations), resultsByYear(evaluations));
    }

    private List<DashboardResponse.CountByLabel> applicantsByInstitution(MongoCollection<Document> evaluations) {
        List<DashboardResponse.CountByLabel> rows = new ArrayList<>();
        for (Document document : aggregate(evaluations, distinctApplicants("$procedencia.tipoInstitucion"))) {
            rows.add(new DashboardResponse.CountByLabel(label(document.get("_id")), size(document.get("aspirantes"))));
        }
        rows.sort(byCount());
        return rows;
    }

    private List<DashboardResponse.CountByLabel> approvedBySubject(MongoCollection<Document> evaluations) {
        List<DashboardResponse.CountByLabel> rows = new ArrayList<>();
        List<Document> pipeline = List.of(new Document("$match", new Document("aprobacion", "APROBADO")),
                new Document("$group", new Document("_id", "$materia").append("cantidad", new Document("$sum", 1))));
        for (Document document : aggregate(evaluations, pipeline)) {
            rows.add(new DashboardResponse.CountByLabel(label(document.get("_id")), number(document.get("cantidad"))));
        }
        rows.sort(byCount());
        return rows;
    }

    private List<DashboardResponse.RateByLabel> approvalBySubject(MongoCollection<Document> evaluations) {
        return rates(evaluations, "$materia");
    }

    private List<DashboardResponse.CountByPair> applicantsBySexAndInstitution(MongoCollection<Document> evaluations) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>();
        Document groupId = new Document("sexo", "$sexo").append("tipo", "$procedencia.tipoInstitucion");
        List<Document> pipeline = List.of(new Document("$group", new Document("_id", groupId)
                .append("aspirantes", new Document("$addToSet", "$aspiranteId"))));
        for (Document document : aggregate(evaluations, pipeline)) {
            Document id = document.get("_id", Document.class);
            rows.add(new DashboardResponse.CountByPair(label(id.get("sexo")), label(id.get("tipo")), size(document.get("aspirantes"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.CountByPair::segundaEtiqueta).thenComparing(DashboardResponse.CountByPair::primeraEtiqueta));
        return rows;
    }

    private List<DashboardResponse.RateByLabel> approvalByInstitution(MongoCollection<Document> evaluations) {
        return rates(evaluations, "$procedencia.tipoInstitucion");
    }

    private List<DashboardResponse.ResultsByYear> resultsByYear(MongoCollection<Document> evaluations) {
        List<DashboardResponse.ResultsByYear> rows = new ArrayList<>();
        List<Document> pipeline = List.of(new Document("$group", new Document("_id", "$anioIngreso")
                .append("evaluaciones", new Document("$sum", 1))
                .append("aprobadas", new Document("$sum", approvalExpression()))));
        for (Document document : aggregate(evaluations, pipeline)) {
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            rows.add(new DashboardResponse.ResultsByYear((int) number(document.get("_id")), approved, total - approved,
                    total, percentage(approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.ResultsByYear::anioIngreso));
        return rows;
    }

    private List<DashboardResponse.RateByLabel> rates(MongoCollection<Document> evaluations, String groupingField) {
        List<DashboardResponse.RateByLabel> rows = new ArrayList<>();
        List<Document> pipeline = List.of(new Document("$group", new Document("_id", groupingField)
                .append("evaluaciones", new Document("$sum", 1))
                .append("aprobadas", new Document("$sum", approvalExpression()))));
        for (Document document : aggregate(evaluations, pipeline)) {
            long total = number(document.get("evaluaciones"));
            long approved = number(document.get("aprobadas"));
            rows.add(new DashboardResponse.RateByLabel(label(document.get("_id")), approved, total, percentage(approved, total)));
        }
        rows.sort(Comparator.comparing(DashboardResponse.RateByLabel::etiqueta));
        return rows;
    }

    private List<Document> distinctApplicants(String field) {
        return List.of(new Document("$group", new Document("_id", field)
                .append("aspirantes", new Document("$addToSet", "$aspiranteId"))));
    }

    private Document approvalExpression() {
        return new Document("$cond", List.of(new Document("$eq", List.of("$aprobacion", "APROBADO")), 1, 0));
    }

    private List<Document> aggregate(MongoCollection<Document> evaluations, List<Document> pipeline) {
        return evaluations.aggregate(pipeline).into(new ArrayList<>());
    }

    private Comparator<DashboardResponse.CountByLabel> byCount() {
        return Comparator.comparing(DashboardResponse.CountByLabel::cantidad).reversed()
                .thenComparing(DashboardResponse.CountByLabel::etiqueta);
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    private int size(Object value) {
        return value instanceof List<?> list ? list.size() : 0;
    }

    private String label(Object value) {
        return value == null ? "SIN DATOS" : value.toString();
    }

    private double percentage(long numerator, long denominator) {
        return denominator == 0 ? 0 : Math.round((10000.0 * numerator) / denominator) / 100.0;
    }
}
