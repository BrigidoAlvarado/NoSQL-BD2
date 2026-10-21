package com.nosql.apiadmisionesusac.etl;

import org.springframework.stereotype.Service;

/** Orquesta una carga y deja un estado recuperable si el segundo motor falla. */
@Service
public class CsvImportService {
    private final CsvEvaluationProcessor processor;
    private final EtlMongoStore mongo;
    private final EtlNeo4jStore neo4j;

    CsvImportService(CsvEvaluationProcessor processor, EtlMongoStore mongo, EtlNeo4jStore neo4j) {
        this.processor = processor;
        this.mongo = mongo;
        this.neo4j = neo4j;
    }

    public ImportResult importFile(String filename, byte[] bytes) {
        String sha256 = EtlMongoStore.sha256(bytes);
        var previous = mongo.completed(sha256);
        if (previous.isPresent()) {
            return previous.get();
        }
        mongo.start(sha256, filename);
        final CsvProcessingResult processed;
        try {
            processed = processor.process(bytes);
        } catch (CsvFormatException exception) {
            String detail = "Línea " + exception.line() + ": " + exception.getMessage();
            mongo.finish(sha256, "RECHAZADA", 0, 0, 0, detail);
            return new ImportResult(sha256, "RECHAZADA", 0, 0, 0, false, detail);
        }
        int read = processed.validRows().size() + processed.rejectedRows().size();
        try {
            mongo.replaceRejections(sha256, processed.rejectedRows());
            if (processed.validRows().isEmpty()) {
                mongo.finish(sha256, "SIN_FILAS_VALIDAS", read, 0, processed.rejectedRows().size(),
                        "El archivo no contiene filas utilizables.");
                return new ImportResult(sha256, "SIN_FILAS_VALIDAS", read, 0, processed.rejectedRows().size(), false,
                        "El archivo no contiene filas utilizables.");
            }
            mongo.saveEvaluations(sha256, filename, processed.validRows());
            mongo.finish(sha256, "MONGO_LISTO", read, processed.validRows().size(), processed.rejectedRows().size(), null);
        } catch (RuntimeException exception) {
            mongo.finish(sha256, "FALLO_MONGO", read, processed.validRows().size(), processed.rejectedRows().size(), exception.getMessage());
            return new ImportResult(sha256, "FALLO_MONGO", read, processed.validRows().size(), processed.rejectedRows().size(), false,
                    "No se pudo guardar la carga en MongoDB.");
        }
        try {
            neo4j.saveEvaluations(processed.validRows());
            mongo.finish(sha256, "COMPLETA", read, processed.validRows().size(), processed.rejectedRows().size(), null);
            return new ImportResult(sha256, "COMPLETA", read, processed.validRows().size(), processed.rejectedRows().size(), false,
                    "Carga sincronizada en MongoDB y Neo4j.");
        } catch (RuntimeException exception) {
            mongo.finish(sha256, "PENDIENTE_NEO4J", read, processed.validRows().size(), processed.rejectedRows().size(), exception.getMessage());
            return new ImportResult(sha256, "PENDIENTE_NEO4J", read, processed.validRows().size(), processed.rejectedRows().size(), false,
                    "MongoDB se completó; Neo4j debe reintentarse con el mismo archivo.");
        }
    }
}
