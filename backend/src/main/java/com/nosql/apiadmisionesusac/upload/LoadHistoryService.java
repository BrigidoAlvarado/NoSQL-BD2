package com.nosql.apiadmisionesusac.upload;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Lecturas paginadas del historial y sus registros, ordenadas de forma estable. */
@Service
public class LoadHistoryService {
    private static final int SIZE = 20;
    private final MongoTemplate mongo;

    public LoadHistoryService(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    public record Page(List<Document> items, long total, int page, int size) {}

    public Page loads(int page) {
        validate(page);
        var collection = mongo.getCollection("cargas");
        var items = collection.find().projection(new Document("_id", 0))
                .sort(new Document("iniciadaEn", -1).append("sha256Archivo", 1))
                .skip(page * SIZE).limit(SIZE).into(new ArrayList<>());
        for (var item : items) {
            Object detail = item.remove("detalleError");
            String state = item.getString("estado");
            if ("RECHAZADA".equals(state) || "SIN_FILAS_VALIDAS".equals(state)) {
                item.put("mensaje", detail);
            } else if ("PENDIENTE_NEO4J".equals(state)) {
                item.put("mensaje", "Sincronización Neo4j pendiente. Reintenta la carga.");
            } else if ("FALLO_MONGO".equals(state)) {
                item.put("mensaje", "No se pudo completar la escritura en MongoDB. Reintenta la carga.");
            }
        }
        return new Page(items, collection.countDocuments(), page, SIZE);
    }

    public Page details(String hash, String kind, int page) {
        validate(page);
        if (!hash.matches("[a-fA-F0-9]{64}") || !List.of("validos", "rechazos").contains(kind)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detalle de carga inválido.");
        }
        boolean valid = kind.equals("validos");
        var collection = mongo.getCollection(valid ? "evaluaciones" : "rechazos_carga");
        var filter = new Document(valid ? "fuentes.sha256Archivo" : "sha256Archivo", hash);
        var order = valid ? new Document("_id", 1) : new Document("fila", 1).append("_id", 1);
        var items = collection.find(filter).sort(order).skip(page * SIZE).limit(SIZE)
                .into(new ArrayList<>());
        for (var item : items) {
            item.remove("_id");
            if (valid) {
                var sources = item.getList("fuentes", Document.class);
                item.put("fuentes", sources.stream()
                        .filter(source -> hash.equals(source.getString("sha256Archivo"))).toList());
            }
        }
        return new Page(items, collection.countDocuments(filter), page, SIZE);
    }

    private void validate(int page) {
        if (page < 0 || page > 100000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página fuera de rango.");
        }
    }
}
