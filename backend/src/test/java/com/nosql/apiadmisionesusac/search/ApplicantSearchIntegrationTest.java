package com.nosql.apiadmisionesusac.search;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Date;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.server.ResponseStatusException;

/** Se ejecuta explícitamente contra MongoDB local: -Dsearch.integration=true. */
@SpringBootTest
@EnabledIfSystemProperty(named = "search.integration", matches = "true")
class ApplicantSearchIntegrationTest {
    private static final String APPLICANT = "C08_VALIDACION_ASPIRANTE";
    @Autowired private ApplicantSearchService search;
    @Autowired private MongoTemplate mongoTemplate;

    @AfterEach
    void cleanup() { mongoTemplate.getCollection("evaluaciones").deleteMany(eq("aspiranteId", APPLICANT)); }

    @Test
    void buscaYConservaLasEvaluacionesDistintasDelAspirante() {
        insert("c08-1", "C08_MATERIA_A", 1, "APROBADO");
        insert("c08-2", "C08_MATERIA_B", 2, "NO APROBADO");

        SearchResponse matches = search.search("materia", "C08_MATERIA_A", 50);
        assertEquals(1, matches.totalDevuelto());
        assertEquals(APPLICANT, matches.resultados().getFirst().aspiranteId());
        ApplicantDetail detail = search.applicant(APPLICANT);
        assertEquals(2, detail.totalEvaluaciones());
        assertEquals("C08_MATERIA_B", detail.evaluaciones().get(1).materia());
        assertEquals(0, search.search("materia", "C08_SIN_COINCIDENCIA", 50).totalDevuelto());
        assertThrows(ResponseStatusException.class, () -> search.search("materia", " ", 50));
    }

    private void insert(String id, String subject, int opportunity, String result) {
        mongoTemplate.getCollection("evaluaciones").insertOne(new Document("_id", id).append("aspiranteId", APPLICANT)
                .append("fechaAsignacion", Date.from(Instant.parse("2024-01-0" + opportunity + "T00:00:00Z")))
                .append("sexo", "F").append("materia", subject).append("oportunidad", opportunity).append("anioIngreso", 2024)
                .append("aprobacion", result).append("carreraObjetivo", "C08_CARRERA")
                .append("procedencia", new Document("tipoInstitucion", "C08_TIPO").append("municipio", "C08_MUNICIPIO").append("departamento", "C08_DEPARTAMENTO")));
    }
}
