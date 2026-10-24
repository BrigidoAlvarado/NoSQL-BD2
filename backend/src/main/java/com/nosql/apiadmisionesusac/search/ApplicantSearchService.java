package com.nosql.apiadmisionesusac.search;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ApplicantSearchService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final Map<String, String> CRITERIA = Map.of(
            "aspirante", "aspiranteId",
            "carrera", "carreraObjetivo",
            "institucion", "procedencia.tipoInstitucion",
            "materia", "materia",
            "municipio", "procedencia.municipio",
            "departamento", "procedencia.departamento");
    private final MongoTemplate mongoTemplate;

    public ApplicantSearchService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public SearchResponse search(String criterio, String termino, Integer requestedLimit) {
        String normalizedCriterion = criterio == null ? "" : criterio.trim().toLowerCase();
        String field = CRITERIA.get(normalizedCriterion);
        if (field == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El criterio de búsqueda no es válido.");
        }
        String normalizedTerm = termino == null ? "" : termino.trim();
        if (normalizedTerm.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un término para realizar la búsqueda.");
        }
        Bson filter = "aspirante".equals(normalizedCriterion)
                ? Filters.eq(field, normalizedTerm)
                : Filters.regex(field, Pattern.compile(Pattern.quote(normalizedTerm), Pattern.CASE_INSENSITIVE));
        List<SearchResponse.EvaluationResult> results = new ArrayList<>();
        evaluations().find(filter).sort(Sorts.orderBy(Sorts.descending("anioIngreso"), Sorts.ascending("fechaAsignacion")))
                .limit(limit(requestedLimit)).forEach(document -> results.add(toSearchResult(document)));
        return new SearchResponse(normalizedCriterion, normalizedTerm, results.size(), results);
    }

    public ApplicantDetail applicant(String aspiranteId) {
        String normalizedId = aspiranteId == null ? "" : aspiranteId.trim();
        if (normalizedId.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correlativo del aspirante es obligatorio.");
        }
        List<ApplicantDetail.Evaluation> evaluations = new ArrayList<>();
        evaluations().find(Filters.eq("aspiranteId", normalizedId))
                .sort(Sorts.orderBy(Sorts.ascending("anioIngreso"), Sorts.ascending("fechaAsignacion"),
                        Sorts.ascending("materia"), Sorts.ascending("oportunidad")))
                .forEach(document -> evaluations.add(toDetail(document)));
        if (evaluations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el aspirante solicitado.");
        }
        int approved = (int) evaluations.stream().filter(row -> "APROBADO".equals(row.aprobacion())).count();
        return new ApplicantDetail(normalizedId, evaluations.size(), approved, evaluations);
    }

    private MongoCollection<Document> evaluations() {
        return mongoTemplate.getCollection("evaluaciones");
    }

    private int limit(Integer value) {
        if (value == null) return DEFAULT_LIMIT;
        if (value < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El límite debe ser mayor que cero.");
        return Math.min(value, MAX_LIMIT);
    }

    private SearchResponse.EvaluationResult toSearchResult(Document d) {
        Document source = d.get("procedencia", Document.class);
        return new SearchResponse.EvaluationResult(d.getString("aspiranteId"), date(d.get("fechaAsignacion")),
                d.getString("materia"), integer(d, "oportunidad"), integer(d, "anioIngreso"), d.getString("aprobacion"),
                d.getString("carreraObjetivo"), nested(source, "tipoInstitucion"), nested(source, "municipio"), nested(source, "departamento"));
    }

    private ApplicantDetail.Evaluation toDetail(Document d) {
        Document source = d.get("procedencia", Document.class);
        return new ApplicantDetail.Evaluation(date(d.get("fechaAsignacion")), d.getString("sexo"), d.getInteger("anioNacimiento"),
                d.getInteger("edadIngreso"), d.getString("materia"), integer(d, "oportunidad"), integer(d, "anioIngreso"),
                d.getString("aprobacion"), d.getString("carreraObjetivo"), nested(source, "tipoInstitucion"),
                nested(source, "municipio"), nested(source, "departamento"));
    }

    private String nested(Document source, String key) { return source == null ? null : source.getString(key); }
    private int integer(Document document, String key) { Number value = document.get(key, Number.class); return value == null ? 0 : value.intValue(); }
    private String date(Object value) {
        if (value instanceof Date date) return Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC).toLocalDate().toString();
        if (value instanceof LocalDate date) return date.toString();
        return value == null ? null : value.toString();
    }
}
