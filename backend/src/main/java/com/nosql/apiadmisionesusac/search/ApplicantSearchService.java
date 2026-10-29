package com.nosql.apiadmisionesusac.search;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.nosql.apiadmisionesusac.search.queries.M15ApplicantInformation;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ApplicantSearchService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;
    private static final Map<String, String> CRITERIA =
            Map.of(
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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El criterio de búsqueda no es válido.");
        }
        String normalizedTerm = termino == null ? "" : termino.trim();
        if (normalizedTerm.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ingresa un término para realizar la búsqueda.");
        }
        Bson filter =
                "aspirante".equals(normalizedCriterion)
                        ? Filters.eq(field, normalizedTerm)
                        : Filters.regex(
                                field,
                                Pattern.compile(
                                        Pattern.quote(normalizedTerm), Pattern.CASE_INSENSITIVE));
        List<SearchResponse.EvaluationResult> results = new ArrayList<>();
        evaluations()
                .find(filter)
                .sort(
                        Sorts.orderBy(
                                Sorts.descending("anioIngreso"),
                                Sorts.ascending("fechaAsignacion")))
                .limit(limit(requestedLimit))
                .forEach(document -> results.add(ApplicantResultMapper.toSearchResult(document)));
        return new SearchResponse(normalizedCriterion, normalizedTerm, results.size(), results);
    }

    public ApplicantDetail applicant(String aspiranteId) {
        String normalizedId = aspiranteId == null ? "" : aspiranteId.trim();
        if (normalizedId.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El correlativo del aspirante es obligatorio.");
        }
        return M15ApplicantInformation.execute(evaluations(), normalizedId);
    }

    private MongoCollection<Document> evaluations() {
        return mongoTemplate.getCollection("evaluaciones");
    }

    private int limit(Integer value) {
        if (value == null) return DEFAULT_LIMIT;
        if (value < 1)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El límite debe ser mayor que cero.");
        return Math.min(value, MAX_LIMIT);
    }
}
