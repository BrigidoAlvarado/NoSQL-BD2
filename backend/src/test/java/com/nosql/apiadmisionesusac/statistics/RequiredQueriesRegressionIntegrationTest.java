package com.nosql.apiadmisionesusac.statistics;

import static org.junit.jupiter.api.Assertions.*;

import com.mongodb.client.MongoCollection;
import com.nosql.apiadmisionesusac.shared.AnalysisFilters;
import com.nosql.apiadmisionesusac.statistics.queries.M16FirstAttemptFailures;
import com.nosql.apiadmisionesusac.statistics.queries.M20ResultsByYear;
import java.util.Map;
import java.util.UUID;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Regresiones de M16/M20 con resultados conocidos, sin modificar las cargas del usuario. */
@SpringBootTest
@EnabledIfSystemProperty(named = "statistics.integration", matches = "true")
class RequiredQueriesRegressionIntegrationTest {
    @Autowired private MongoTemplate mongo;
    private MongoCollection<Document> evaluations;

    @BeforeEach
    void createFixture() {
        evaluations = mongo.getCollection("validacion_m16_m20_" + UUID.randomUUID());
        evaluations.insertMany(QueryFixture.evaluations());
    }

    @AfterEach
    void cleanFixture() {
        if (evaluations != null) evaluations.drop();
    }

    @Test
    void m16RespetaFiltrosCombinadosYConservaAspirantesUnicos() {
        // Otra evaluación reprobada de a1 no debe contarlo como otra persona.
        Document repeatedApplicant = new Document(QueryFixture.evaluations().getFirst());
        repeatedApplicant.put("_id", "e5");
        repeatedApplicant.put("materia", "FISICA");
        evaluations.insertOne(repeatedApplicant);
        assertEquals(2, M16FirstAttemptFailures.execute(evaluations).getFirst().cantidad());
        Document filter = AnalysisFilters.mongo(Map.of(
                "anio", "2022", "carrera", "A", "tipoInstitucion", "PUBLICO", "sexo", "F"));
        var result = M16FirstAttemptFailures.execute(evaluations, filter);
        assertEquals(1, result.size());
        assertEquals("A", result.getFirst().etiqueta());
        assertEquals(1, result.getFirst().cantidad());
        assertTrue(M16FirstAttemptFailures.execute(evaluations,
                AnalysisFilters.mongo(Map.of("anio", "2099"))).isEmpty());
        assertTrue(M16FirstAttemptFailures.execute(evaluations,
                AnalysisFilters.mongo(Map.of("carrera", "B"))).isEmpty());
        assertTrue(M16FirstAttemptFailures.execute(evaluations,
                AnalysisFilters.mongo(Map.of("resultado", "APROBADO"))).isEmpty());
    }

    @Test
    void m20CalculaAmbosPorcentajesConElTotalDeCadaAnioYDelSubconjunto() {
        Document approved = new Document(QueryFixture.evaluations().get(1));
        approved.put("_id", "e5");
        approved.put("anioIngreso", 2023);
        evaluations.insertOne(approved);
        var years = M20ResultsByYear.execute(evaluations);
        assertEquals(2, years.size());
        assertEquals(2022, years.getFirst().anioIngreso());
        assertEquals(4, years.getFirst().evaluaciones());
        assertEquals(50, years.getFirst().porcentajeAprobacion());
        assertEquals(50, years.getFirst().porcentajeNoAprobacion());
        assertEquals(2023, years.getLast().anioIngreso());
        assertEquals(100, years.getLast().porcentajeAprobacion());
        assertEquals(0, years.getLast().porcentajeNoAprobacion());
        var publicYear = M20ResultsByYear.execute(evaluations,
                AnalysisFilters.mongo(Map.of("tipoInstitucion", "PUBLICO", "anio", "2022"))).getFirst();
        assertEquals(3, publicYear.evaluaciones());
        assertEquals(2, publicYear.aprobadas());
        assertEquals(1, publicYear.noAprobadas());
        assertEquals(66.67, publicYear.porcentajeAprobacion(), 0.001);
        assertEquals(33.33, publicYear.porcentajeNoAprobacion(), 0.001);
        var privateYear = M20ResultsByYear.execute(evaluations,
                AnalysisFilters.mongo(Map.of("tipoInstitucion", "PRIVADO"))).getFirst();
        assertEquals(0, privateYear.porcentajeAprobacion());
        assertEquals(100, privateYear.porcentajeNoAprobacion());
        assertTrue(M20ResultsByYear.execute(evaluations,
                AnalysisFilters.mongo(Map.of("anio", "2099"))).isEmpty());
    }
}
