package com.nosql.apiadmisionesusac.etl;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
class EtlMongoStore {
    private static final String CARGAS = "cargas";
    private final MongoTemplate mongoTemplate;

    EtlMongoStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    Optional<ImportResult> completed(String sha256) {
        Document load = mongoTemplate.getCollection(CARGAS).find(eq("sha256Archivo", sha256)).first();
        if (load == null || !"COMPLETA".equals(load.getString("estado"))) {
            return Optional.empty();
        }
        return Optional.of(new ImportResult(sha256, "COMPLETA", integer(load, "filasLeidas"),
                integer(load, "filasValidas"), integer(load, "filasRechazadas"), true,
                "El mismo contenido ya fue sincronizado en MongoDB y Neo4j."));
    }

    void start(String sha256, String filename) {
        mongoTemplate.getCollection(CARGAS).updateOne(eq("sha256Archivo", sha256), new Document("$set", new Document()
                .append("estado", "RECIBIDA")
                .append("nombreArchivo", filename)
                .append("iniciadaEn", Date.from(Instant.now()))
                .append("detalleError", null))
                .append("$setOnInsert", new Document("sha256Archivo", sha256)), new UpdateOptions().upsert(true));
    }

    void replaceRejections(String sha256, List<RejectedRow> rejectedRows) {
        var collection = mongoTemplate.getCollection("rechazos_carga");
        collection.deleteMany(eq("sha256Archivo", sha256));
        if (rejectedRows.isEmpty()) {
            return;
        }
        List<Document> documents = new ArrayList<>();
        for (RejectedRow rejected : rejectedRows) {
            documents.add(new Document("sha256Archivo", sha256)
                    .append("fila", rejected.line())
                    .append("codigo", rejected.code())
                    .append("detalle", rejected.detail())
                    .append("valores", rejected.values())
                    .append("registradoEn", Date.from(Instant.now())));
        }
        collection.insertMany(documents);
    }

    void saveEvaluations(String sha256, String filename, List<Evaluation> evaluations) {
        List<WriteModel<Document>> operations = new ArrayList<>();
        for (Evaluation evaluation : evaluations) {
            Document source = new Document("sha256Archivo", sha256).append("nombreArchivo", filename)
                    .append("fila", evaluation.filaOrigen());
            Document update = new Document("$setOnInsert", evaluationDocument(evaluation))
                    .append("$addToSet", new Document("fuentes", source));
            operations.add(new UpdateOneModel<>(eq("_id", evaluation.id()), update, new UpdateOptions().upsert(true)));
            if (operations.size() == 500) {
                mongoTemplate.getCollection("evaluaciones").bulkWrite(operations);
                operations.clear();
            }
        }
        if (!operations.isEmpty()) {
            mongoTemplate.getCollection("evaluaciones").bulkWrite(operations);
        }
    }

    void finish(String sha256, String state, int read, int valid, int rejected, String detail) {
        mongoTemplate.getCollection(CARGAS).updateOne(eq("sha256Archivo", sha256), new Document("$set", new Document()
                .append("estado", state)
                .append("filasLeidas", read)
                .append("filasValidas", valid)
                .append("filasRechazadas", rejected)
                .append("detalleError", detail)
                .append("actualizadaEn", Date.from(Instant.now()))));
    }

    private Document evaluationDocument(Evaluation value) {
        Document procedencia = new Document("departamento", value.departamentoInstitucionEducativa())
                .append("municipio", value.municipioInstitucionEducativa())
                .append("tipoInstitucion", value.tipoInstitucionEducativa())
                .append("departamentoConocido", value.departamentoInstitucionEducativaConocido())
                .append("municipioConocido", value.municipioInstitucionEducativaConocido())
                .append("tipoConocido", value.tipoInstitucionEducativaConocido());
        return new Document("_id", value.id())
                .append("aspiranteId", value.aspiranteId())
                .append("fechaAsignacion", Date.from(value.fechaAsignacion().atStartOfDay().toInstant(ZoneOffset.UTC)))
                .append("sexo", value.sexo())
                .append("anioNacimiento", value.anioNacimiento())
                .append("anioNacimientoConocido", value.anioNacimientoConocido())
                .append("edadIngreso", value.edadIngreso())
                .append("materia", value.materia())
                .append("oportunidad", value.oportunidad())
                .append("anioIngreso", value.anioIngreso())
                .append("aprobacion", value.aprobacion())
                .append("carreraObjetivo", value.carreraObjetivo())
                .append("carreraObjetivoConocida", value.carreraObjetivoConocida())
                .append("procedencia", procedencia)
                .append("esquemaVersion", 1);
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 no disponible", exception);
        }
    }

    private int integer(Document document, String key) {
        Number value = document.get(key, Number.class);
        return value == null ? 0 : value.intValue();
    }
}
