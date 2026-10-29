package com.nosql.apiadmisionesusac.upload;

import com.mongodb.client.MongoClient;
import com.nosql.apiadmisionesusac.statistics.queries.*;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Base temporal independiente: no escribe ni elimina datos del proyecto. */
@SpringBootTest
@EnabledIfSystemProperty(named = "additional.integration", matches = "true")
class AdditionalAnalysisIntegrationTest {
    @Autowired MongoClient client;

    @Test
    void compruebaConsultasYMasDeCienRechazos() {
        var mongo = new MongoTemplate(client, "prueba_adicional_" + UUID.randomUUID().toString().replace("-", ""));
        try {
            var evaluations = mongo.getCollection("evaluaciones");
            evaluations.insertMany(List.of(
                    evaluation("A", "MATEMATICA", 1, "NO APROBADO", 0),
                    evaluation("A", "MATEMATICA", 2, "APROBADO", 10),
                    evaluation("A", "FISICA", 1, "APROBADO", 10),
                    evaluation("B", "MATEMATICA", 1, "NO APROBADO", 0)));
            var m21 = M21AdditionalAnalysis.execute(evaluations);
            var first = m21.stream().filter(row -> row.get("_id", Document.class).getString("materia").equals("MATEMATICA")
                    && row.get("_id", Document.class).getInteger("oportunidad") == 1).findFirst().orElseThrow();
            assertEquals(2, ((Number) first.get("total")).intValue());
            assertEquals(0.0, ((Number) first.get("porcentaje")).doubleValue());
            var recovery = M22AdditionalAnalysis.execute(evaluations).getFirst();
            assertEquals(1, ((Number) recovery.get("recuperados")).intValue());
            assertEquals(50.0, ((Number) recovery.get("porcentaje")).doubleValue());
            var distribution = M23AdditionalAnalysis.execute(evaluations);
            assertEquals(2, distribution.size());
            assertTrue(distribution.stream().allMatch(row -> ((Number) row.get("porcentaje")).doubleValue() == 50));
            var days = M24AdditionalAnalysis.execute(evaluations).getFirst();
            assertEquals(10.0, ((Number) days.get("promedioDias")).doubleValue());
            assertEquals(1, ((Number) days.get("excluidos")).intValue());
            var quality = M25AdditionalAnalysis.execute(evaluations).getFirst();
            assertEquals(100.0, ((Number) quality.get("porcentajeSinEdad")).doubleValue());
            assertEquals(100.0, ((Number) quality.get("porcentajeCarreraDesconocida")).doubleValue());
            assertEquals(100.0, ((Number) quality.get("porcentajeProcedenciaDesconocida")).doubleValue());
            verifyHistory(mongo);
        } finally {
            mongo.getDb().drop();
        }
    }

    private void verifyHistory(MongoTemplate mongo) {
        String hash = "a".repeat(64);
        var rejected = mongo.getCollection("rechazos_carga");
        for (int line = 1; line <= 121; line++) {
            rejected.insertOne(new Document("sha256Archivo", hash).append("fila", line).append("valores", new Document()));
        }
        var history = new LoadHistoryService(mongo);
        Set<Integer> lines = new HashSet<>();
        for (int page = 0; page < 7; page++) {
            var response = history.details(hash, "rechazos", page);
            assertEquals(121, response.total());
            for (var row : response.items()) assertTrue(lines.add(row.getInteger("fila")));
        }
        assertEquals(121, lines.size());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> history.loads(-1));
        mongo.getCollection("evaluaciones").updateMany(new Document(), new Document("$set", new Document("fuentes", List.of(
                new Document("sha256Archivo", hash).append("fila", 1),
                new Document("sha256Archivo", "b".repeat(64)).append("fila", 2)))));
        var valid = history.details(hash, "validos", 0);
        assertEquals(4, valid.total());
        assertTrue(valid.items().stream().allMatch(row -> row.getList("fuentes", Document.class).size() == 1));
    }

    private Document evaluation(String person, String subject, int opportunity, String result, int days) {
        return new Document("aspiranteId", person).append("materia", subject).append("oportunidad", opportunity)
                .append("anioIngreso", 2024).append("aprobacion", result)
                .append("fechaAsignacion", new Date(1704067200000L + days * 86400000L))
                .append("edadIngreso", null).append("carreraObjetivoConocida", false)
                .append("procedencia", new Document("departamentoConocido", false).append("municipioConocido", false).append("tipoConocido", false));
    }
}
