package com.nosql.apiadmisionesusac.statistics;

import com.nosql.apiadmisionesusac.statistics.queries.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.bson.Document;
import java.util.List;

/** Acceso a las cinco consultas adicionales de lectura. */
@RestController
@RequestMapping("/api/estadisticas/adicionales")
public class AdditionalStatisticsController {
    private final MongoTemplate mongo;
    public AdditionalStatisticsController(MongoTemplate mongo) { this.mongo = mongo; }
    @GetMapping("/{id}")
    public List<Object> query(@PathVariable String id, @RequestParam java.util.Map<String, String> parameters) {
        var filter = com.nosql.apiadmisionesusac.shared.AnalysisFilters.mongo(parameters);
        var collection = mongo.getCollection("evaluaciones");
        List<Document> rows = switch (id) {
            case "M21" -> M21AdditionalAnalysis.execute(collection, filter);
            case "M22" -> M22AdditionalAnalysis.execute(collection, filter);
            case "M23" -> M23AdditionalAnalysis.execute(collection, filter);
            case "M24" -> M24AdditionalAnalysis.execute(collection, filter);
            case "M25" -> M25AdditionalAnalysis.execute(collection, filter);
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta no disponible.");
        };
        return rows.stream().map(row -> AdditionalStatisticsResponse.from(id, row)).toList();
    }
}
