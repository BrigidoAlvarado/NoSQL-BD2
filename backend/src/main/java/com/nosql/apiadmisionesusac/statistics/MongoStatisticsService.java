package com.nosql.apiadmisionesusac.statistics;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                applicantsBySexAndInstitution(evaluations), approvalByInstitution(evaluations), resultsByYear(evaluations),
                approvedByCareerYear(evaluations), ageByCareer(evaluations), refreshCareerSummary(evaluations),
                ageApprovedByCareerInstitution(evaluations), approvedByMunicipalityCareer(evaluations), publicByMonthSubject(evaluations),
                topYoungCareers(evaluations), approvalByAge(evaluations), opportunitiesBySubject(evaluations),
                firstAttemptFailures(evaluations), topMunicipalities(evaluations), careersByDepartment(evaluations));
    }

    private List<DashboardResponse.CareerYearCount> approvedByCareerYear(MongoCollection<Document> e) {
        List<DashboardResponse.CareerYearCount> rows = new ArrayList<>();
        Document id = new Document("carrera", "$carreraObjetivo").append("anio", "$anioIngreso");
        for (Document d : aggregate(e, List.of(new Document("$match", new Document("aprobacion", "APROBADO")),
                new Document("$group", new Document("_id", id).append("aspirantes", new Document("$addToSet", "$aspiranteId")))))) {
            Document key = d.get("_id", Document.class);
            rows.add(new DashboardResponse.CareerYearCount(label(key.get("carrera")), (int) number(key.get("anio")), size(d.get("aspirantes"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.CareerYearCount::anioIngreso).thenComparing(DashboardResponse.CareerYearCount::carrera));
        return rows;
    }

    private List<DashboardResponse.AverageByLabel> ageByCareer(MongoCollection<Document> e) {
        List<DashboardResponse.AverageByLabel> rows = new ArrayList<>();
        List<Document> pipeline = firstAgePipeline(null, new Document("carrera", "$_id.carrera"));
        for (Document d : aggregate(e, pipeline)) rows.add(averageLabel(d));
        rows.sort(Comparator.comparing(DashboardResponse.AverageByLabel::etiqueta));
        return rows;
    }

    private List<DashboardResponse.AverageByPair> ageApprovedByCareerInstitution(MongoCollection<Document> e) {
        List<DashboardResponse.AverageByPair> rows = new ArrayList<>();
        Document grouped = new Document("carrera", "$_id.carrera").append("tipo", "$_id.tipo");
        for (Document d : aggregate(e, firstAgePipeline(new Document("aprobacion", "APROBADO"), grouped))) {
            Document id = d.get("_id", Document.class);
            rows.add(new DashboardResponse.AverageByPair(label(id.get("carrera")), label(id.get("tipo")),
                    decimal(d.get("promedio")), number(d.get("conDato")), number(d.get("sinDato"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.AverageByPair::primeraEtiqueta).thenComparing(DashboardResponse.AverageByPair::segundaEtiqueta));
        return rows;
    }

    private List<Document> firstAgePipeline(Document match, Document finalId) {
        List<Document> p = new ArrayList<>();
        if (match != null) p.add(new Document("$match", match));
        p.add(new Document("$sort", new Document("anioIngreso", 1).append("fechaAsignacion", 1)));
        Document person = new Document("aspirante", "$aspiranteId").append("carrera", "$carreraObjetivo");
        if (match != null) person.append("tipo", "$procedencia.tipoInstitucion");
        p.add(new Document("$group", new Document("_id", person).append("edad", new Document("$first", "$edadIngreso"))));
        p.add(new Document("$group", new Document("_id", finalId)
                .append("promedio", new Document("$avg", "$edad"))
                .append("conDato", new Document("$sum", presentExpression("$edad")))
                .append("sinDato", new Document("$sum", absentExpression("$edad")))));
        return p;
    }

    private DashboardResponse.AverageByLabel averageLabel(Document d) {
        return new DashboardResponse.AverageByLabel(label(d.get("_id")), decimal(d.get("promedio")), number(d.get("conDato")), number(d.get("sinDato")));
    }

    private List<DashboardResponse.CareerSummary> refreshCareerSummary(MongoCollection<Document> e) {
        List<DashboardResponse.CareerSummary> result = new ArrayList<>();
        Document id = new Document("anio", "$anioIngreso").append("carrera", "$carreraObjetivo");
        List<Document> pipeline = List.of(new Document("$group", new Document("_id", id)
                .append("evaluaciones", new Document("$sum", 1)).append("aspirantes", new Document("$addToSet", "$aspiranteId"))
                .append("aprobadas", new Document("$sum", approvalExpression())).append("sinEdad", new Document("$sum", absentExpression("$edadIngreso")))));
        MongoCollection<Document> summary = mongoTemplate.getCollection("resumen_carrera");
        for (Document d : aggregate(e, pipeline)) {
            Document key = d.get("_id", Document.class); long total = number(d.get("evaluaciones")); long approved = number(d.get("aprobadas"));
            DashboardResponse.CareerSummary row = new DashboardResponse.CareerSummary((int) number(key.get("anio")), label(key.get("carrera")), total,
                    size(d.get("aspirantes")), approved, total - approved, percentage(approved, total), number(d.get("sinEdad")));
            result.add(row);
            summary.replaceOne(eq("_id", row.anioIngreso() + "\u001F" + row.carrera()), new Document("_id", row.anioIngreso() + "\u001F" + row.carrera())
                    .append("anioIngreso", row.anioIngreso()).append("carreraObjetivo", row.carrera()).append("evaluaciones", row.evaluaciones())
                    .append("aspirantesDistintos", row.aspirantesDistintos()).append("aprobadas", row.aprobadas()).append("noAprobadas", row.noAprobadas())
                    .append("porcentajeAprobacion", row.porcentajeAprobacion()).append("evaluacionesSinEdad", row.sinEdad()), new ReplaceOptions().upsert(true));
        }
        result.sort(Comparator.comparing(DashboardResponse.CareerSummary::anioIngreso).thenComparing(DashboardResponse.CareerSummary::carrera));
        return result;
    }

    private List<DashboardResponse.CountByTriple> approvedByMunicipalityCareer(MongoCollection<Document> e) {
        List<DashboardResponse.CountByTriple> rows = new ArrayList<>();
        Document id = new Document("departamento", "$procedencia.departamento").append("municipio", "$procedencia.municipio").append("carrera", "$carreraObjetivo");
        for (Document d : aggregate(e, List.of(new Document("$match", new Document("aprobacion", "APROBADO")), new Document("$group", new Document("_id", id).append("aspirantes", new Document("$addToSet", "$aspiranteId")))))) {
            Document key = d.get("_id", Document.class); rows.add(new DashboardResponse.CountByTriple(label(key.get("departamento")), label(key.get("municipio")), label(key.get("carrera")), size(d.get("aspirantes"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.CountByTriple::cantidad).reversed()); return rows;
    }

    private List<DashboardResponse.CountByPair> publicByMonthSubject(MongoCollection<Document> e) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>();
        Document id = new Document("mes", new Document("$dateToString", new Document("format", "%Y-%m").append("date", "$fechaAsignacion"))).append("materia", "$materia");
        for (Document d : aggregate(e, List.of(new Document("$match", new Document("procedencia.tipoInstitucion", "PUBLICO")), new Document("$group", new Document("_id", id).append("cantidad", new Document("$sum", 1)))))) {
            Document key = d.get("_id", Document.class); rows.add(new DashboardResponse.CountByPair(label(key.get("mes")), label(key.get("materia")), number(d.get("cantidad"))));
        }
        rows.sort(Comparator.comparing(DashboardResponse.CountByPair::primeraEtiqueta).thenComparing(DashboardResponse.CountByPair::segundaEtiqueta)); return rows;
    }

    private List<DashboardResponse.CountByLabel> topYoungCareers(MongoCollection<Document> e) {
        List<Document> p = new ArrayList<>(); p.add(new Document("$sort", new Document("anioIngreso", 1))); p.add(new Document("$group", new Document("_id", new Document("aspirante", "$aspiranteId").append("carrera", "$carreraObjetivo")).append("edad", new Document("$first", "$edadIngreso")).append("conocida", new Document("$first", "$carreraObjetivoConocida"))));
        p.add(new Document("$match", new Document("edad", new Document("$gte", 16).append("$lte", 18)).append("conocida", true))); p.add(new Document("$group", new Document("_id", "$_id.carrera").append("cantidad", new Document("$sum", 1))));
        return countRows(e, p).stream().limit(5).toList();
    }

    private List<DashboardResponse.AgeRate> approvalByAge(MongoCollection<Document> e) {
        List<DashboardResponse.AgeRate> rows = new ArrayList<>();
        for (Document d : aggregate(e, List.of(new Document("$match", new Document("edadIngreso", new Document("$ne", null))), new Document("$group", new Document("_id", "$edadIngreso").append("evaluaciones", new Document("$sum", 1)).append("aprobadas", new Document("$sum", approvalExpression())))))) {
            long total = number(d.get("evaluaciones")), approved = number(d.get("aprobadas")); rows.add(new DashboardResponse.AgeRate((int) number(d.get("_id")), approved, total, percentage(approved, total)));
        } rows.sort(Comparator.comparing(DashboardResponse.AgeRate::edad)); return rows;
    }

    private List<DashboardResponse.AverageOpportunities> opportunitiesBySubject(MongoCollection<Document> e) {
        List<DashboardResponse.AverageOpportunities> rows = new ArrayList<>();
        Document opportunity = new Document("aspirante", "$aspiranteId").append("anio", "$anioIngreso").append("materia", "$materia").append("oportunidad", "$oportunidad");
        Document group = new Document("aspirante", "$_id.aspirante").append("anio", "$_id.anio").append("materia", "$_id.materia");
        List<Document> p = List.of(new Document("$group", new Document("_id", opportunity)), new Document("$group", new Document("_id", group).append("usadas", new Document("$sum", 1))), new Document("$group", new Document("_id", "$_id.materia").append("promedio", new Document("$avg", "$usadas")).append("grupos", new Document("$sum", 1))));
        for (Document d : aggregate(e, p)) rows.add(new DashboardResponse.AverageOpportunities(label(d.get("_id")), decimal(d.get("promedio")), number(d.get("grupos"))));
        rows.sort(Comparator.comparing(DashboardResponse.AverageOpportunities::materia)); return rows;
    }

    private List<DashboardResponse.CountByLabel> firstAttemptFailures(MongoCollection<Document> e) {
        return countRows(e, List.of(new Document("$match", new Document("oportunidad", 1).append("aprobacion", "NO APROBADO").append("carreraObjetivoConocida", true)), new Document("$group", new Document("_id", "$carreraObjetivo").append("aspirantes", new Document("$addToSet", "$aspiranteId")))) , "aspirantes");
    }

    private List<DashboardResponse.CountByPair> topMunicipalities(MongoCollection<Document> e) {
        List<DashboardResponse.CountByPair> rows = new ArrayList<>(); Document id = new Document("departamento", "$procedencia.departamento").append("municipio", "$procedencia.municipio");
        Document match = new Document("procedencia.departamentoConocido", true).append("procedencia.municipioConocido", true);
        for (Document d : aggregate(e, List.of(new Document("$match", match), new Document("$group", new Document("_id", id).append("aspirantes", new Document("$addToSet", "$aspiranteId")))))) { Document key=d.get("_id",Document.class); rows.add(new DashboardResponse.CountByPair(label(key.get("departamento")),label(key.get("municipio")),size(d.get("aspirantes")))); }
        rows.sort(Comparator.comparing(DashboardResponse.CountByPair::cantidad).reversed().thenComparing(DashboardResponse.CountByPair::segundaEtiqueta)); return rows.stream().limit(5).toList();
    }

    private List<DashboardResponse.CountByPair> careersByDepartment(MongoCollection<Document> e) {
        Map<String, List<DashboardResponse.CountByPair>> grouped = new LinkedHashMap<>(); Document id=new Document("departamento", "$procedencia.departamento").append("carrera", "$carreraObjetivo"); Document match=new Document("procedencia.departamentoConocido",true).append("carreraObjetivoConocida",true);
        for (Document d: aggregate(e,List.of(new Document("$match",match),new Document("$group",new Document("_id",id).append("aspirantes",new Document("$addToSet","$aspiranteId")))))) { Document key=d.get("_id",Document.class); String dept=label(key.get("departamento")); grouped.computeIfAbsent(dept,k->new ArrayList<>()).add(new DashboardResponse.CountByPair(dept,label(key.get("carrera")),size(d.get("aspirantes")))); }
        List<DashboardResponse.CountByPair> result=new ArrayList<>(); for(List<DashboardResponse.CountByPair> choices:grouped.values()){ long max=choices.stream().mapToLong(DashboardResponse.CountByPair::cantidad).max().orElse(0); choices.stream().filter(c->c.cantidad()==max).forEach(result::add); } result.sort(Comparator.comparing(DashboardResponse.CountByPair::primeraEtiqueta).thenComparing(DashboardResponse.CountByPair::segundaEtiqueta)); return result;
    }

    private List<DashboardResponse.CountByLabel> countRows(MongoCollection<Document> e, List<Document> pipeline) { return countRows(e,pipeline,"cantidad"); }
    private List<DashboardResponse.CountByLabel> countRows(MongoCollection<Document> e, List<Document> pipeline, String count) { List<DashboardResponse.CountByLabel> rows=new ArrayList<>(); for(Document d:aggregate(e,pipeline)) rows.add(new DashboardResponse.CountByLabel(label(d.get("_id")), count.equals("aspirantes")?size(d.get(count)):number(d.get(count)))); rows.sort(byCount()); return rows; }

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

    private Document presentExpression(String field) {
        return new Document("$cond", List.of(new Document("$ne", java.util.Arrays.asList(field, null)), 1, 0));
    }

    private Document absentExpression(String field) {
        return new Document("$cond", List.of(new Document("$eq", java.util.Arrays.asList(field, null)), 1, 0));
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

    private double decimal(Object value) {
        return value instanceof Number number ? Math.round(number.doubleValue() * 100.0) / 100.0 : 0;
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
